#!/usr/bin/env python3
"""
RSSB Content Scraper
Downloads audio content and generates catalog JSON files.
"""

import os
import json
import requests
import re
from bs4 import BeautifulSoup
from urllib.parse import urljoin, quote, unquote
import time
import argparse
import sys
from tqdm import tqdm

BASE_URL = "https://rssb.org"
OUTPUT_DIR = "./downloads"

def get_clean_filename(url):
    """Extracts filename from URL, stripping query params and fragments."""
    path = url.split('/')[-1]
    return unquote(path.split('#')[0].split('?')[0])

def parse_time_fragment(url):
    """Parses time fragment like #t=1:04:51 into seconds."""
    if '#t=' not in url:
        return 0

    time_str = url.split('#t=')[1].split('?')[0]  # Handle potential query params after fragment? unlikely but safe
    parts = time_str.split(':')

    seconds = 0
    if len(parts) == 3: # HH:MM:SS
        seconds = int(parts[0]) * 3600 + int(parts[1]) * 60 + int(float(parts[2]))
    elif len(parts) == 2: # MM:SS
        seconds = int(parts[0]) * 60 + int(float(parts[1]))
    elif len(parts) == 1: # SS
        seconds = int(float(parts[0]))

    return seconds

class ScraperStats:
    def __init__(self):
        self.total = 0
        self.downloaded = 0
        self.skipped = 0
        self.failed = 0

    def __str__(self):
        return (f"Scraper Summary:\n"
                f"  Total Files Processed: {self.total}\n"
                f"  Downloaded: {self.downloaded}\n"
                f"  Skipped (Existed): {self.skipped}\n"
                f"  Failed: {self.failed}")

stats = ScraperStats()

def setup_args():
    parser = argparse.ArgumentParser(description="Scrape RSSB audio content")
    parser.add_argument("--limit", type=int, default=0, help="Limit number of items per category (0 for all)")
    parser.add_argument("--dry-run", action="store_true", help="Do not download files, just generate catalogs")
    # Add a max-size option for verification to avoid filling up disk
    parser.add_argument("--max-size", type=int, default=0, help="Max file size to download in bytes (0 for unlimited). Useful for verification.")
    return parser.parse_args()

def download_file(url, output_path, dry_run=False, max_size=0):
    """Download a file with progress and retries."""
    stats.total += 1

    if dry_run:
        tqdm.write(f"  [Dry Run] Would download: {url} -> {output_path}")
        stats.downloaded += 1 # Count as downloaded in dry run
        return

    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    if os.path.exists(output_path):
        stats.skipped += 1
        return

    temp_path = output_path + ".part"
    retries = 3
    attempt = 0

    while attempt < retries:
        try:
            resp = requests.get(url, stream=True, timeout=30)
            resp.raise_for_status()
            total_size = int(resp.headers.get('content-length', 0))

            with open(temp_path, 'wb') as f, tqdm(
                desc=os.path.basename(output_path),
                total=total_size,
                unit='B',
                unit_scale=True,
                unit_divisor=1024,
                leave=False
            ) as bar:
                downloaded = 0
                for chunk in resp.iter_content(chunk_size=8192):
                    f.write(chunk)
                    bar.update(len(chunk))
                    downloaded += len(chunk)
                    if max_size > 0 and downloaded >= max_size:
                        bar.write(f"  [Limit] Stopped after {downloaded} bytes.")
                        break

            # Rename temp file to final filename on success
            os.rename(temp_path, output_path)
            stats.downloaded += 1
            return # Success

        except Exception as e:
            attempt += 1
            tqdm.write(f"  Error downloading {url} (Attempt {attempt}/{retries}): {e}")
            if os.path.exists(temp_path):
                os.remove(temp_path)

            if attempt < retries:
                time.sleep(5)
            else:
                tqdm.write(f"  Failed to download {url} after {retries} attempts.")
                stats.failed += 1

def get_soup(url):
    """Helper to get BeautifulSoup object."""
    try:
        resp = requests.get(url, timeout=30)
        resp.raise_for_status()
        # Force UTF-8 encoding if not correctly detected
        resp.encoding = 'utf-8'
        return BeautifulSoup(resp.text, 'html.parser')
    except Exception as e:
        tqdm.write(f"Error fetching {url}: {e}")
        return None

def scrape_audiobooks(limit, dry_run, max_size):
    """Scrape audiobook listing and download chapters."""
    url = f"{BASE_URL}/audiobooks.html"
    soup = get_soup(url)
    if not soup: return []

    audiobooks = []

    book_links = []
    for link in soup.select('a[href^="audio-"]'):
        href = link['href']
        if href.endswith('.html'):
            title = link.text.strip()
            book_links.append((title, urljoin(BASE_URL, href)))

    book_links = list(set(book_links))
    book_links.sort(key=lambda x: x[0])

    if limit > 0:
        book_links = book_links[:limit]

    tqdm.write(f"Found {len(book_links)} audiobooks to process.")

    # Use tqdm for the batch progress
    for title, book_url in tqdm(book_links, desc="Audiobooks", unit="book"):
        book_soup = get_soup(book_url)
        if not book_soup: continue

        chapters = []
        chapter_links = book_soup.select('a[data-url]')
        book_id = book_url.split('/')[-1].replace('.html', '').replace('audio-', '')

        # First pass: collect basic info and download
        temp_chapters = []
        for i, link in enumerate(chapter_links):
            file_rel_path = link['data-url']
            chapter_title = link.text.strip() or f"Chapter {i+1}"

            file_url = urljoin(BASE_URL, file_rel_path)
            clean_filename = get_clean_filename(file_rel_path)
            local_path = os.path.join(OUTPUT_DIR, "audio", "audiobooks", book_id, clean_filename)

            # Extract start time from the original relative path (which contains the fragment)
            start_time = parse_time_fragment(file_rel_path)

            download_file(file_url, local_path, dry_run, max_size)

            temp_chapters.append({
                "id": f"{book_id}-{i+1:02d}",
                "title": chapter_title,
                "trackNumber": i + 1,
                "streamUrl": f"audio/audiobooks/{book_id}/{clean_filename}",
                "startTime": start_time,
                "filename": clean_filename # Store filename to group by file later
            })

        # Second pass: calculate end times
        # Group by filename to handle multi-file audiobooks correctly
        # (Though most seem to be single-file, this is safer)
        chapters_by_file = {}
        for ch in temp_chapters:
            fname = ch['filename']
            if fname not in chapters_by_file:
                chapters_by_file[fname] = []
            chapters_by_file[fname].append(ch)

        final_chapters = []
        for fname, file_chapters in chapters_by_file.items():
            # Sort by track number (should be already sorted by HTML order, but good to be safe)
            file_chapters.sort(key=lambda x: x['trackNumber'])

            for i in range(len(file_chapters)):
                current_chap = file_chapters[i]
                next_chap = file_chapters[i+1] if i + 1 < len(file_chapters) else None

                # If there is a next chapter in the same file, its start time is this chapter's end time
                if next_chap:
                    current_chap['endTime'] = next_chap['startTime']
                else:
                    # Last chapter in the file (or single chapter)
                    current_chap['endTime'] = None

                # Remove internal helper key
                del current_chap['filename']
                final_chapters.append(current_chap)

        # Sort all chapters back by track number
        final_chapters.sort(key=lambda x: x['trackNumber'])

        audiobooks.append({
            "id": book_id,
            "title": title,
            "type": "AUDIOBOOK",
            "sourceUrl": book_url,
            "chapters": final_chapters
        })

        # time.sleep(0.5) # Removed to speed up execution, tqdm handles pacing well enough visually

    return audiobooks

def scrape_qna(limit, dry_run, max_size):
    """Get Q&A session list."""
    url = f"{BASE_URL}/QandA.html"
    soup = get_soup(url)
    if not soup: return []

    sessions = []
    links = soup.select('a[data-url]')

    if limit > 0:
        links = links[:limit]

    tqdm.write(f"Found {len(links)} Q&A sessions to process.")

    for i, link in enumerate(tqdm(links, desc="Q&A Sessions", unit="session")):
        file_rel_path = link['data-url']
        file_url = urljoin(BASE_URL, file_rel_path)

        filename = file_rel_path.split('/')[-1]
        clean_filename = f"{i+1:03d}.mp3"
        local_path = os.path.join(OUTPUT_DIR, "audio", "qna", clean_filename)

        download_file(file_url, local_path, dry_run, max_size)

        sessions.append({
            "id": f"qna-{i+1:03d}",
            "title": f"Q&A Session {i+1}",
            "type": "QNA",
            "trackNumber": i + 1,
            "streamUrl": f"audio/qna/{clean_filename}"
        })

    return sessions

def scrape_shabads(limit, dry_run, max_size):
    """Scrape shabad listing."""
    url = f"{BASE_URL}/shabads.html"
    soup = get_soup(url)
    if not soup: return []

    shabads = []
    links = soup.select('a[href*="audio/shabads"]')
    mp3_links = [l for l in links if l['href'].endswith('.mp3')]

    if limit > 0:
        mp3_links = mp3_links[:limit]

    tqdm.write(f"Found {len(mp3_links)} shabads to process.")

    for link in tqdm(mp3_links, desc="Shabads", unit="track"):
        href = link['href']
        file_url = urljoin(BASE_URL, href)

        filename = get_clean_filename(href)
        decoded_filename = filename

        name_part = decoded_filename.rsplit('.', 1)[0]
        if ' - ' in name_part:
            title, mystic = name_part.split(' - ', 1)
        else:
            title = name_part
            mystic = "Unknown"

        shabad_id = "".join(c for c in title if c.isalnum()).lower()

        clean_filename = f"{shabad_id}.mp3"
        local_path = os.path.join(OUTPUT_DIR, "audio", "shabads", clean_filename)

        download_file(file_url, local_path, dry_run, max_size)

        shabads.append({
            "id": shabad_id,
            "title": title.strip(),
            "type": "SHABAD",
            "mystic": mystic.strip(),
            "streamUrl": f"audio/shabads/{clean_filename}"
        })

    return shabads

def scrape_discourses(limit, dry_run, max_size):
    """Scrape discourses."""
    url = f"{BASE_URL}/discourses-en.html"
    soup = get_soup(url)
    if not soup: return []

    discourses = []
    links = soup.select('a[data-url]')

    if limit > 0:
        links = links[:limit]

    tqdm.write(f"Found {len(links)} discourses to process.")

    for i, link in enumerate(tqdm(links, desc="Discourses", unit="track")):
        file_rel_path = link['data-url']
        file_url = urljoin(BASE_URL, file_rel_path)

        filename = get_clean_filename(file_rel_path)

        raw_title = link.text.strip()
        if '. ' in raw_title:
            title = raw_title.split('. ', 1)[1]
        else:
            title = raw_title

        local_path = os.path.join(OUTPUT_DIR, "audio", "discourses", "en", filename)

        download_file(file_url, local_path, dry_run, max_size)

        discourses.append({
            "id": f"discourse-en-{i+1:03d}",
            "title": title,
            "type": "DISCOURSE_MASTER",
            "language": "en",
            "streamUrl": f"audio/discourses/en/{filename}"
        })

    return discourses

def generate_catalog(content, output_file):
    """Generate catalog JSON file."""
    os.makedirs(os.path.dirname(output_file), exist_ok=True)

    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(content, f, indent=2, ensure_ascii=False)

    tqdm.write(f"Generated catalog: {output_file}")

def main():
    args = setup_args()

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Audiobooks
    tqdm.write("\n=== Scraping Audiobooks ===")
    audiobooks = scrape_audiobooks(args.limit, args.dry_run, args.max_size)
    generate_catalog(audiobooks, f"{OUTPUT_DIR}/catalog/audiobooks.json")

    # Q&A
    tqdm.write("\n=== Scraping Q&A ===")
    qna = scrape_qna(args.limit, args.dry_run, args.max_size)
    generate_catalog(qna, f"{OUTPUT_DIR}/catalog/qna.json")

    # Shabads
    tqdm.write("\n=== Scraping Shabads ===")
    shabads = scrape_shabads(args.limit, args.dry_run, args.max_size)
    generate_catalog(shabads, f"{OUTPUT_DIR}/catalog/shabads.json")

    # Discourses
    tqdm.write("\n=== Scraping Discourses ===")
    discourses = scrape_discourses(args.limit, args.dry_run, args.max_size)
    generate_catalog(discourses, f"{OUTPUT_DIR}/catalog/discourses.json")

    tqdm.write("\n=== Done! ===")
    print(stats)

if __name__ == "__main__":
    main()
