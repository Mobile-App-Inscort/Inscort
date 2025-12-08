# services/crawler_service.py
from crawler.instagram_crawler import InstagramCrawler
from crawler.image_downloader import download_and_upload_to_s3
from services.s3_service import generate_presigned_url
import os

class CrawlerService:

    @staticmethod
    def crawl_and_upload(url: str):
        cookie_path = os.getenv("COOKIE_PATH")
        crawler = InstagramCrawler(headless=True)
        crawler.load_cookies(cookie_path)

        # 1) 크롤링
        image_urls = crawler.crawl(url)
        crawler.close()

        # 2) post_id 추출
        try:
            post_id = url.split("/p/")[1].split("/")[0]
        except:
            raise ValueError("Invalid Instagram post URL format")

        # 3) S3 prefix 생성
        prefix = f"instagram/posts/{post_id}"

        # 4) 업로드
        s3_keys = download_and_upload_to_s3(image_urls, prefix)

        # ⭐ 5) presigned URL 만들기
        images = []
        for key in s3_keys:
            presigned = generate_presigned_url(key)
            images.append({
                "key": key,
                "url": presigned
            })

        # 6) 결과 반환
        return {
            "postId": post_id,
            "images": images
        }
