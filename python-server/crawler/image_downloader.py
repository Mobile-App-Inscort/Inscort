# python-server/crawler/downloader.py
import requests
from pathlib import Path
from aws.s3_utils import upload_to_s3     # 새로 만든 S3 유틸

def fetch_image_bytes(url: str) -> bytes:
    response = requests.get(url, timeout=10)
    response.raise_for_status()
    return response.content

def download_and_upload_to_s3(url_list, prefix: str):
    uploaded_keys = []

    for idx, url in enumerate(url_list):
        img_bytes = fetch_image_bytes(url)
        key = f"{prefix}/image_{idx}.jpg"
        upload_to_s3(img_bytes, key)
        uploaded_keys.append(key)

    return uploaded_keys

