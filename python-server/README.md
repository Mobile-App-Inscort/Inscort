📁 프로젝트 구조
python-server/
 ├── app.py
 ├── services/
 │    └── crawler_service.py
 ├── crawler/
 │    ├── instagram_crawler.py
 │    ├── image_downloader.py
 │    └── cookies/
 │          └── prod_cookies.json   (🚫 Git 미포함)
 ├── aws/
 │    └── s3_utils.py
 ├── requirements.txt
 ├── .env                (🚫 Git 미포함)
 └── venv/               (로컬 가상환경, Git 미포함)

⚠️ 사전 요구사항
✔ Python 3.11.x

반드시 Python 3.11 사용해야 합니다.
undetected-chromedriver는 Python 3.12를 지원하지 않습니다.

macOS 설치 방법:

brew install python@3.11

✔ Chrome 브라우저 설치

undetected-chromedriver는 Chrome 자동 실행이 필요합니다.

✔ AWS IAM User (S3 접근용)

✔ Instagram 로그인 쿠키 필요 (중요)

로그인하지 않으면 슬라이드 이미지 전체를 볼 수 없기 때문에
쿠키 파일이 반드시 필요합니다.

파일 위치:

python-server/crawler/cookies/cookies.json

🚫 Git에 포함되지 않으며 팀 노션에 업로드 해놨습니다.

🛠 실행 방법
1) Python 3.11 기반 가상환경 생성
python3.11 -m venv venv
source venv/bin/activate

Windows:

venv\Scripts\activate

2) 패키지 설치
pip install -r requirements.txt

3) venv 안에서 서버 실행
python app.py
기본 포트: 9000
