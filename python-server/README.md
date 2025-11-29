# 📘 Instagram Crawler Flask Server – Setup Guide

이 문서는 Instagram 게시물 이미지 크롤링 → S3 업로드를 수행하는  
Flask 기반 python-server 실행 방법을 정리한 가이드입니다.

---

## 📁 프로젝트 구조

```
python-server/
 ├── app.py
 ├── services/
 │    └── crawler_service.py
 ├── crawler/
 │    ├── instagram_crawler.py
 │    ├── image_downloader.py
 │    └── cookies/
 │          └── prod_cookies.json   (Git 미포함)
 ├── aws/
 │    └── s3_utils.py
 ├── requirements.txt
 ├── .env                (Git 미포함)
 └── venv/               (로컬 가상환경, Git 미포함)
```

---

## ⚠️ 사전 요구사항

### 1) Python 3.11.x 필수  
undetected-chromedriver는 Python 3.12를 지원하지 않기 때문에  
반드시 Python 3.11 버전을 사용해야 합니다.

macOS 설치 명령어:

```
brew install python@3.11
```

---

### 2) Chrome 브라우저 설치  
undetected-chromedriver는 Chrome 기반으로 동작합니다.

---

### 3) AWS IAM User 
Access Key / Secret Key가 필요합니다.

---

### 4) Instagram 로그인 쿠키 필요  
로그인하지 않은 계정으로는 슬라이드 이미지 전체를 수집할 수 없습니다.  
따라서 로그인 쿠키가 반드시 필요합니다.

쿠키 파일 위치:

```
python-server/crawler/cookies/cookies.json
```

Git에 포함되지 않은 파일들은 팀 노션에 업로드되어 있습니다.

---

## 실행 방법

### 1) Python 3.11 기반 가상환경 생성

```
python3.11 -m venv venv
source venv/bin/activate
```

Windows:

```
venv\Scripts\activate
```

---

### 2) 패키지 설치

```
pip install -r requirements.txt
```

---

### 3) 서버 실행

```
python app.py
```

기본 포트:  
http://localhost:9000

---

