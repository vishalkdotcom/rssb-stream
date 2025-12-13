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

# Load environment variables
load_dotenv()

def setup_args():
    parser = argparse.ArgumentParser(description="Upload content to R2")
    parser.add_argument("--source", default="./downloads", help="Source directory")
    parser.add_argument("--bucket", default="rssb-stream", help="R2 Bucket name")
    parser.add_argument("--dry-run", action="store_true", default=True, help="Dry run (default)")
    parser.add_argument("--no-dry-run", action="store_false", dest="dry_run", help="Execute real upload")
    return parser.parse_args()

def get_s3_client():
    endpoint_url = os.getenv("R2_ENDPOINT_URL")
    access_key = os.getenv("R2_ACCESS_KEY_ID")
    secret_key = os.getenv("R2_SECRET_ACCESS_KEY")

    if not all([endpoint_url, access_key, secret_key]):
        print("Error: Missing R2 credentials. Please set R2_ENDPOINT_URL, R2_ACCESS_KEY_ID, and R2_SECRET_ACCESS_KEY.")
        return None

    return boto3.client(
        's3',
        endpoint_url=endpoint_url,
        aws_access_key_id=access_key,
        aws_secret_access_key=secret_key
    )

def upload_file(s3, bucket, local_path, s3_key, dry_run):
    """Upload a file if it doesn't exist or size differs."""

    try:
        local_size = os.path.getsize(local_path)
    except OSError:
        print(f"  Error reading local file: {local_path}")
        return

    # Check if file exists on S3
    try:
        head = s3.head_object(Bucket=bucket, Key=s3_key)
        remote_size = head['ContentLength']

        if local_size == remote_size:
            print(f"  Skipping (synced): {s3_key}")
            return
        else:
            print(f"  Update needed: {s3_key} (Local: {local_size}, Remote: {remote_size})")

    except ClientError as e:
        # 404 Not Found means we need to upload
        if e.response['Error']['Code'] != "404":
            print(f"  Error checking {s3_key}: {e}")
            return

    if dry_run:
        print(f"  [Dry Run] Would upload: {local_path} -> s3://{bucket}/{s3_key}")
        return

    print(f"  Uploading: {s3_key}")
    try:
        content_type, _ = mimetypes.guess_type(local_path)
        if content_type is None:
            content_type = 'application/octet-stream'

        with open(local_path, "rb") as f:
            s3.upload_fileobj(
                f,
                bucket,
                s3_key,
                ExtraArgs={'ContentType': content_type}
            )
    except Exception as e:
        print(f"  Failed to upload {local_path}: {e}")

def main():
    args = setup_args()

    if not os.path.exists(args.source):
        print(f"Source directory not found: {args.source}")
        return

    s3 = None
    if not args.dry_run:
        s3 = get_s3_client()
        if not s3:
            return
    else:
        print("=== DRY RUN MODE ===")

    # Walk through the directory
    for root, dirs, files in os.walk(args.source):
        for file in files:
            local_path = os.path.join(root, file)

            # Compute S3 key (relative path)
            rel_path = os.path.relpath(local_path, args.source)
            # Ensure forward slashes for S3 keys regardless of OS
            s3_key = rel_path.replace(os.path.sep, '/')

            if s3:
                upload_file(s3, args.bucket, local_path, s3_key, args.dry_run)
            else:
                # In dry run without credentials, we assume we would upload everything
                # or we skip the "check remote" part if we can't connect.
                # But to properly simulate dry run WITH remote check, we need credentials.
                # If no credentials in dry run, just print "Would upload".
                print(f"  [Dry Run] Would upload: {local_path} -> s3://{args.bucket}/{s3_key}")

if __name__ == "__main__":
    main()
