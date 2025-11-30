# python-server/crawler/instagram_crawler.py

import json
import time
from pathlib import Path
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class InstagramCrawler:
    def __init__(self, headless=True):
        options = uc.ChromeOptions()
        if headless:
            options.add_argument("--headless=new")
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        self.driver = uc.Chrome(options=options)

    def load_cookies(self, cookie_path=None):
        cookie_file = Path(cookie_path)
        if not cookie_file.exists():
            print("[WARN] Cookie file not found.")
            return

        cookies = json.loads(cookie_file.read_text())

        domains = ["https://www.instagram.com/", "https://instagram.com/"]

        for d in domains:
            self.driver.get(d)
            time.sleep(1)

            for cookie in cookies:
                cookie.pop("sameSite", None)
                cookie.pop("expirationDate", None)

                if cookie.get("domain") and cookie["domain"].startswith("."):
                    cookie["domain"] = cookie["domain"][1:]
                try:
                    self.driver.add_cookie(cookie)
                except:
                    pass

        self.driver.get("https://www.instagram.com/")
        time.sleep(1)
        print("[INFO] Cookies loaded")

    def fetch_html(self, url: str) -> str:
        print(f"[INFO] Fetching {url}")
        self.driver.get(url)

        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.TAG_NAME, "article"))
            )
        except:
            print("[WARN] article not found")

        time.sleep(1)
        return self.driver.page_source

    def collect_images(self):
        img_urls = set()

        try:
            wrapper = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "div[role='presentation']"))
            )
        except:
            print("[ERROR] slider wrapper not found")
            return []

        while True:
            time.sleep(0.8)
            imgs = wrapper.find_elements(By.TAG_NAME, "img")
            for img in imgs:
                src = img.get_attribute("src")
                if src and "scontent" in src:
                    img_urls.add(src)

            # next 버튼 클릭
            try:
                next_btn = self.driver.find_element(
                    By.CSS_SELECTOR,
                    "button[aria-label='Next'], button[aria-label='다음']"
                )
                next_btn.click()
            except:
                break

        return list(img_urls)

    def crawl(self, url: str) -> list[str]:
        """전체 크롤링 플로우"""
        self.fetch_html(url)
        return self.collect_images()

    def close(self):
        self.driver.quit()
