#!/usr/bin/env python3
"""
RSSB Content Scraper
Downloads audio content and generates catalog JSON files.
"""

import os
import json
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin, quote
import time
import argparse
import sys

BASE_URL = "https://rssb.org"
OUTPUT_DIR = "./downloads"

def setup_args():
    parser = argparse.ArgumentParser(description="Scrape RSSB audio content")
    parser.add_argument("--limit", type=int, default=0, help="Limit number of items per category (0 for all)")
    parser.add_argument("--dry-run", action="store_true", help="Do not download files, just generate catalogs")
    # Add a max-size option for verification to avoid filling up disk
    parser.add_argument("--max-size", type=int, default=0, help="Max file size to download in bytes (0 for unlimited). Useful for verification.")
    return parser.parse_args()

def download_file(url, output_path, dry_run=False, max_size=0):
    """Download a file with progress."""
    if dry_run:
        print(f"  [Dry Run] Would download: {url} -> {output_path}")
        return

    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    if os.path.exists(output_path):
        print(f"  Skipping (exists): {output_path}")
        return

    print(f"  Downloading: {url}")
    temp_path = output_path + ".part"
    try:
        resp = requests.get(url, stream=True)
        resp.raise_for_status()

        with open(temp_path, 'wb') as f:
            downloaded = 0
            for chunk in resp.iter_content(chunk_size=8192):
                f.write(chunk)
                downloaded += len(chunk)
                if max_size > 0 and downloaded >= max_size:
                    print(f"  [Limit] Stopped after {downloaded} bytes.")
                    break

        # Rename temp file to final filename on success
        os.rename(temp_path, output_path)
    except Exception as e:
        print(f"  Error downloading {url}: {e}")
        if os.path.exists(temp_path):
            os.remove(temp_path)

def get_soup(url):
    """Helper to get BeautifulSoup object."""
    print(f"Fetching {url}...")
    try:
        resp = requests.get(url)
        resp.raise_for_status()
        # Force UTF-8 encoding if not correctly detected
        resp.encoding = 'utf-8'
        return BeautifulSoup(resp.text, 'html.parser')
    except Exception as e:
        print(f"Error fetching {url}: {e}")
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

    print(f"Found {len(book_links)} audiobooks to process.")

    for title, book_url in book_links:
        print(f"Processing Book: {title}")
        book_soup = get_soup(book_url)
        if not book_soup: continue

        chapters = []
        chapter_links = book_soup.select('a[data-url]')

        # If limiting, maybe limit chapters too? But usually limit applies to items (books).
        # We will download all chapters of the limited books.

        for i, link in enumerate(chapter_links):
            file_rel_path = link['data-url']
            chapter_title = link.text.strip() or f"Chapter {i+1}"

            file_url = urljoin(BASE_URL, file_rel_path)

            book_id = book_url.split('/')[-1].replace('.html', '').replace('audio-', '')
            filename = file_rel_path.split('/')[-1]
            local_path = os.path.join(OUTPUT_DIR, "audio", "audiobooks", book_id, filename)

            download_file(file_url, local_path, dry_run, max_size)

            chapters.append({
                "id": f"{book_id}-{i+1:02d}",
                "title": chapter_title,
                "trackNumber": i + 1,
                "streamUrl": f"audio/audiobooks/{book_id}/{filename}"
            })

        audiobooks.append({
            "id": book_id,
            "title": title,
            "type": "AUDIOBOOK",
            "sourceUrl": book_url,
            "chapters": chapters
        })

        time.sleep(0.5)

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

    print(f"Found {len(links)} Q&A sessions to process.")

    for i, link in enumerate(links):
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

    print(f"Found {len(mp3_links)} shabads to process.")

    for link in mp3_links:
        href = link['href']
        file_url = urljoin(BASE_URL, href)

        filename = href.split('/')[-1]
        decoded_filename = requests.utils.unquote(filename)

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

    print(f"Found {len(links)} discourses to process.")

    for i, link in enumerate(links):
        file_rel_path = link['data-url']
        file_url = urljoin(BASE_URL, file_rel_path)

        filename = file_rel_path.split('/')[-1]

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

    print(f"Generated catalog: {output_file}")

def main():
    args = setup_args()

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Audiobooks
    print("\n=== Scraping Audiobooks ===")
    audiobooks = scrape_audiobooks(args.limit, args.dry_run, args.max_size)
    generate_catalog(audiobooks, f"{OUTPUT_DIR}/catalog/audiobooks.json")

    # Q&A
    print("\n=== Scraping Q&A ===")
    qna = scrape_qna(args.limit, args.dry_run, args.max_size)
    generate_catalog(qna, f"{OUTPUT_DIR}/catalog/qna.json")

    # Shabads
    print("\n=== Scraping Shabads ===")
    shabads = scrape_shabads(args.limit, args.dry_run, args.max_size)
    generate_catalog(shabads, f"{OUTPUT_DIR}/catalog/shabads.json")

    # Discourses
    print("\n=== Scraping Discourses ===")
    discourses = scrape_discourses(args.limit, args.dry_run, args.max_size)
    generate_catalog(discourses, f"{OUTPUT_DIR}/catalog/discourses.json")

    print("\n=== Done! ===")

if __name__ == "__main__":
    main()
