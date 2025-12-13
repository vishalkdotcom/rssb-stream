#!/usr/bin/env python3
"""
RSSB Content Manager
Orchestrates scraping and uploading of content.
"""

import os
import argparse
import subprocess
import sys

def setup_args():
    parser = argparse.ArgumentParser(description="RSSB Content Manager")
    parser.add_argument("--limit", type=int, default=0, help="Limit number of items per category for scraper")
    parser.add_argument("--max-size", type=int, default=0, help="Max file size for scraper (verification)")
    parser.add_argument("--bucket", default="rssb-stream", help="R2 Bucket name")
    parser.add_argument("--dry-run", action="store_true", default=False, help="Dry run mode for both scraper and uploader")
    parser.add_argument("--only-scrape", action="store_true", help="Run only the scraper")
    parser.add_argument("--only-upload", action="store_true", help="Run only the uploader")
    parser.add_argument("--no-ssl-verify", action="store_true", help="Disable SSL verification for uploader")
    return parser.parse_args()

def run_script(script_name, args):
    """Run a python script with arguments."""
    cmd = [sys.executable, script_name] + args
    print(f"\n>>> Running {script_name} with args: {args}")
    try:
        subprocess.check_call(cmd)
    except subprocess.CalledProcessError as e:
        print(f"Error running {script_name}: {e}")
        sys.exit(1)

def main():
    args = setup_args()

    base_dir = os.path.dirname(os.path.abspath(__file__))
    scraper_script = os.path.join(base_dir, "scraper.py")
    uploader_script = os.path.join(base_dir, "uploader.py")

    # 1. Scraper
    if not args.only_upload:
        scraper_args = []
        if args.limit > 0:
            scraper_args.extend(["--limit", str(args.limit)])
        if args.max_size > 0:
            scraper_args.extend(["--max-size", str(args.max_size)])
        if args.dry_run:
            scraper_args.append("--dry-run")

        run_script(scraper_script, scraper_args)

    # 2. Uploader
    if not args.only_scrape:
        uploader_args = []
        if args.bucket:
            uploader_args.extend(["--bucket", args.bucket])

        # Logic: If --dry-run is passed to manager, we pass it to uploader.
        # But uploader defaults to dry-run=True. To force real upload, we need --no-dry-run.
        if args.dry_run:
            uploader_args.append("--dry-run")
        else:
            uploader_args.append("--no-dry-run")

        if args.no_ssl_verify:
            uploader_args.append("--no-ssl-verify")

        run_script(uploader_script, uploader_args)

    print("\n>>> All tasks completed.")

if __name__ == "__main__":
    main()
