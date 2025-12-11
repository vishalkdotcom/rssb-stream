# Cloudflare R2 Setup Guide

This guide walks through setting up Cloudflare R2 for hosting RSSB Stream audio content.

## Prerequisites
- Cloudflare account (free tier works)
- Node.js installed (for Wrangler CLI)

---

## Step 1: Create Cloudflare Account

1. Go to [dash.cloudflare.com](https://dash.cloudflare.com)
2. Sign up or log in
3. Navigate to **R2 Object Storage** in the sidebar

---

## Step 2: Create R2 Bucket

1. Click **Create bucket**
2. Name: `rssb-stream` (or your preference)
3. Location: Choose nearest region (e.g., Asia Pacific for India)
4. Click **Create bucket**

---

## Step 3: Enable Public Access

For streaming, you need public read access:

1. Go to your bucket settings
2. Under **Public access**, click **Allow Access**
3. Choose one of:
   - **R2.dev subdomain** (easiest): Enable to get a URL like `https://pub-xxxxx.r2.dev`
   - **Custom domain**: Connect your own domain

> **Note**: R2.dev subdomain is fine for personal use. No additional Cloudflare plan needed.

---

## Step 4: Install Wrangler CLI

```bash
npm install -g wrangler
```

Login to Cloudflare:
```bash
wrangler login
```

---

## Step 5: Configure Wrangler

Create `wrangler.toml` in your project:

```toml
name = "rssb-stream"
compatibility_date = "2024-01-01"

[[r2_buckets]]
binding = "BUCKET"
bucket_name = "rssb-stream"
```

---

## Step 6: Upload Content

### Upload a single file:
```bash
wrangler r2 object put rssb-stream/audio/qna/001.mp3 --file ./downloads/qna-001.mp3
```

### Upload entire directory:
```bash
# From your downloads folder
for file in audio/**/*.mp3; do
  wrangler r2 object put "rssb-stream/$file" --file "$file"
done
```

### Using rclone (alternative, faster for bulk):
```bash
# Install rclone
# Configure R2: https://developers.cloudflare.com/r2/examples/rclone/

rclone sync ./downloads/audio r2:rssb-stream/audio --progress
```

---

## Step 7: Get Your Public URL

After enabling public access, your files will be available at:
```
https://pub-{ACCOUNT_ID}.r2.dev/audio/qna/001.mp3
```

Or with custom domain:
```
https://stream.yourdomain.com/audio/qna/001.mp3
```

---

## R2 Bucket Structure

Organize your content like this:

```
rssb-stream/
├── catalog/
│   ├── audiobooks.json    # Metadata
│   ├── qna.json
│   ├── shabads.json
│   └── discourses.json
├── audio/
│   ├── audiobooks/
│   │   ├── awareness-of-the-divine/
│   │   │   ├── 01.mp3
│   │   │   └── ...
│   │   └── ...
│   ├── qna/
│   │   ├── 001.mp3
│   │   └── ...
│   ├── shabads/
│   │   └── ...
│   └── discourses/
│       ├── en/
│       ├── hi/
│       └── ...
└── thumbnails/
    └── ...
```

---

## Catalog JSON Format

Example `catalog/qna.json`:
```json
[
  {
    "id": "qna-001",
    "title": "Q&A Session 1",
    "type": "QNA",
    "duration": 3600,
    "streamUrl": "https://pub-xxx.r2.dev/audio/qna/001.mp3",
    "description": "Maharaj Charan Singh answers questions",
    "dateAdded": 1702300800
  }
]
```

---

## Cost Estimate (Personal Use)

R2 Free Tier includes:
- **10 GB** storage/month
- **10 million** Class A operations (writes)
- **1 million** Class B operations (reads)
- **Egress**: Free! (No bandwidth charges)

For ~5GB of audio content with personal streaming, you'll likely stay within free tier.

---

## Next Steps

1. ✅ Set up R2 bucket
2. Download content from rssb.org (manual or scraper)
3. Upload to R2
4. Create catalog JSON files
5. Configure app with your R2 URL
