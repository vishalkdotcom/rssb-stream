#!/usr/bin/env python3
"""
RSSB Content Uploader
Syncs downloaded content to Cloudflare R2 (S3 compatible).
"""

import os
import argparse
import mimetypes
import boto3
from botocore.exceptions import NoCredentialsError, ClientError
from dotenv import load_dotenv
from tqdm import tqdm
import threading

# Load environment variables
load_dotenv()

class UploaderStats:
    def __init__(self):
        self.total = 0
        self.uploaded = 0
        self.skipped = 0
        self.failed = 0

    def __str__(self):
        return (f"Uploader Summary:\n"
                f"  Total Files Processed: {self.total}\n"
                f"  Uploaded: {self.uploaded}\n"
                f"  Skipped (Synced): {self.skipped}\n"
                f"  Failed: {self.failed}")

stats = UploaderStats()

class ProgressPercentage(object):
    def __init__(self, filename):
        self._filename = filename
        self._size = float(os.path.getsize(filename))
        self._seen_so_far = 0
        self._lock = threading.Lock()
        self._pbar = tqdm(
            desc=os.path.basename(filename),
            total=self._size,
            unit='B',
            unit_scale=True,
            unit_divisor=1024,
            leave=False
        )

    def __call__(self, bytes_amount):
        # To simplify, we just update the pbar
        with self._lock:
            self._seen_so_far += bytes_amount
            self._pbar.update(bytes_amount)

    def close(self):
        self._pbar.close()

def setup_args():
    parser = argparse.ArgumentParser(description="Upload content to R2")
    parser.add_argument("--source", default="./downloads", help="Source directory")
    parser.add_argument("--bucket", default="rssb-stream", help="R2 Bucket name")
    parser.add_argument("--dry-run", action="store_true", default=True, help="Dry run (default)")
    parser.add_argument("--no-dry-run", action="store_false", dest="dry_run", help="Execute real upload")
    parser.add_argument("--no-ssl-verify", action="store_true", help="Disable SSL verification (insecure, for testing)")
    return parser.parse_args()

def get_s3_client(verify_ssl=True):
    endpoint_url = os.getenv("R2_ENDPOINT_URL")
    access_key = os.getenv("R2_ACCESS_KEY_ID")
    secret_key = os.getenv("R2_SECRET_ACCESS_KEY")

    if not all([endpoint_url, access_key, secret_key]):
        tqdm.write("Error: Missing R2 credentials. Please set R2_ENDPOINT_URL, R2_ACCESS_KEY_ID, and R2_SECRET_ACCESS_KEY.")
        return None

    if not verify_ssl:
        import urllib3
        urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    return boto3.client(
        's3',
        endpoint_url=endpoint_url,
        aws_access_key_id=access_key,
        aws_secret_access_key=secret_key,
        verify=verify_ssl
    )

def upload_file(s3, bucket, local_path, s3_key, dry_run):
    """Upload a file if it doesn't exist or size differs."""
    stats.total += 1

    try:
        local_size = os.path.getsize(local_path)
    except OSError:
        tqdm.write(f"  Error reading local file: {local_path}")
        stats.failed += 1
        return

    # Check if file exists on S3
    try:
        head = s3.head_object(Bucket=bucket, Key=s3_key)
        remote_size = head['ContentLength']

        if local_size == remote_size:
            stats.skipped += 1
            return # Skip silently or maybe debug log
        else:
            tqdm.write(f"  Update needed: {s3_key} (Local: {local_size}, Remote: {remote_size})")

    except ClientError as e:
        # 404 Not Found means we need to upload
        if e.response['Error']['Code'] != "404":
            tqdm.write(f"  Error checking {s3_key}: {e}")
            stats.failed += 1
            return

    if dry_run:
        tqdm.write(f"  [Dry Run] Would upload: {local_path} -> s3://{bucket}/{s3_key}")
        stats.uploaded += 1 # Count as uploaded in dry run context
        return

    # Real Upload
    try:
        content_type, _ = mimetypes.guess_type(local_path)
        if content_type is None:
            content_type = 'application/octet-stream'

        progress = ProgressPercentage(local_path)
        with open(local_path, "rb") as f:
            s3.upload_fileobj(
                f,
                bucket,
                s3_key,
                Callback=progress,
                ExtraArgs={'ContentType': content_type}
            )
        progress.close()
        stats.uploaded += 1
    except Exception as e:
        tqdm.write(f"  Failed to upload {local_path}: {e}")
        stats.failed += 1

def main():
    args = setup_args()

    if not os.path.exists(args.source):
        tqdm.write(f"Source directory not found: {args.source}")
        return

    s3 = None
    if not args.dry_run:
        s3 = get_s3_client(verify_ssl=not args.no_ssl_verify)
        if not s3:
            return
    else:
        tqdm.write("=== DRY RUN MODE ===")

    # Gather all files first to use tqdm for the main loop
    file_list = []
    for root, dirs, files in os.walk(args.source):
        for file in files:
            file_list.append(os.path.join(root, file))

    tqdm.write(f"Found {len(file_list)} files to process.")

    for local_path in tqdm(file_list, desc="Processing Files", unit="file"):
        # Compute S3 key (relative path)
        rel_path = os.path.relpath(local_path, args.source)
        # Ensure forward slashes for S3 keys regardless of OS
        s3_key = rel_path.replace(os.path.sep, '/')

        if s3:
            upload_file(s3, args.bucket, local_path, s3_key, args.dry_run)
        else:
            # Dry run without credentials
            tqdm.write(f"  [Dry Run] Would upload: {local_path} -> s3://{args.bucket}/{s3_key}")
            stats.total += 1
            stats.uploaded += 1

    print("\n=== Done! ===")
    print(stats)

if __name__ == "__main__":
    main()
