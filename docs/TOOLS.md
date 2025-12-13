# RSSB Content Tools

This directory contains tools for scraping and uploading RSSB audio content.

## Installation

1. Install Python 3.
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Configuration

For uploading content to Cloudflare R2, set the following environment variables (e.g., in a `.env` file):

```
R2_ENDPOINT_URL=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
R2_ACCESS_KEY_ID=<YOUR_ACCESS_KEY>
R2_SECRET_ACCESS_KEY=<YOUR_SECRET_KEY>
```

## Usage

### 1. Manager (Orchestrator)

The `tools/manager.py` script runs the entire workflow: downloads content (scraper) and syncs it to R2 (uploader).

```bash
# Run everything (Dry Run by default, just prints what would happen)
python tools/manager.py --dry-run

# Run real scraping and uploading
python tools/manager.py --limit 10

# Disable SSL Verification (if you have certification issues, e.g. in some containers)
python tools/manager.py --no-ssl-verify
```

**Common Flags:**
- `--limit N`: Limit to N items per category (e.g., 1 book, 1 Q&A session).
- `--max-size N`: Limit downloads to N bytes (useful for testing without full downloads).
- `--only-scrape`: Skip the upload step.
- `--only-upload`: Skip the scrape step.
- `--bucket NAME`: Specify a custom R2 bucket name (default: `rssb-stream`).

### 2. Scraper

Can be run independently:
```bash
python tools/scraper.py --limit 5
```
Downloads content to `./downloads/`.

### 3. Uploader

Can be run independently:
```bash
python tools/uploader.py --no-dry-run --source ./downloads
```
Syncs `./downloads/` to the R2 bucket.

## Notes

- **Resumability**: The scraper downloads to temporary `.part` files and renames them only on success.
- **Smart Sync**: The uploader checks file size on R2 before uploading. If the file exists and has the same size, it is skipped.
