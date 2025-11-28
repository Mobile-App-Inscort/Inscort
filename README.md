# 📍 Inscort – SNS 기반 데이트 코스 자동 생성 앱

**Instagram 장소 정보 → 지도 코스 자동화**

인스타그램의 ‘맛집/카페 모음’ 게시물 링크를 입력하면,
OCR + 검색 API를 통해 **장소 정보를 자동 추출**하고
지도로 시각화하여 **나만의 데이트 코스**를 손쉽게 만드는 Android 앱입니다.

---

# 🚀 프로젝트 개요 (Overview)

SNS의 감성적인 장소 탐색을 실제 지도 코스로 연결하는 서비스입니다.

**핵심 기능**

1. **인스타 링크 분석**

   * 인스타 게시물(피드/캐러셀) 링크 입력 → 이미지/텍스트 자동 수집 (Selenium)
   * OCR로 장소명/키워드 추출
2. **지도 기반 후보 장소 누적**

   * 여러 게시물에서 추출한 장소들을 지도에서 한눈에 비교
   * 마커 누적 표시
3. **코스 빌더**

   * 후보 중 원하는 장소만 선택
   * Drag & Drop으로 순서 편집
   * Firestore에 저장
4. **코스 상세**

   * 실제 최적 경로(Polyline) 시각화
   * 약속 잡기 + 카카오톡 공유

---

# 🧱 기술 스택 (Tech Stack)

## Android (Client)

* Kotlin
* **MVVM + Jetpack ViewModel + StateFlow**
* Retrofit2 (서버 & Kakao API)
* Coroutines
* Jetpack Compose / XML
* Firebase Authentication
* Firestore (Cloud Firestore)
* Kakao Map SDK
* Kakao Local / Mobility API

## Backend (Server)

* Python
* Flask
* Selenium (인스타 이미지 & 텍스트 크롤링)
* Requests / BeautifulSoup (보조 처리를 위한)

## Infra / External Services

* Firebase ML Kit (OCR)
* Firebase Authentication
* Firestore
* Kakao API (검색 / 경로탐색 / 지도)

---

# 📁 프로젝트 구조 (Repository Structure)

```plaintext
Inscort/
│
├── android-app/          # Android Studio 프로젝트
│   ├── app/
│   ├── gradle/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── ...
│
├── backend/              # Flask + Selenium 서버
│   ├── app.py
│   ├── requirements.txt
│   └── ...
│
├── docs/                 # IA, 플로우차트, Firestore 설계, API 문서
│
├── .github/              # PR/이슈 템플릿
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
│
└── README.md
```

---

# 👥 팀 구성 (Roles)

| 이름  | 역할            | 담당 범위                                            |
| --- | ------------- | ------------------------------------------------ |
| 조윤경 | Backend       | Python, Flask, Selenium (인스타 크롤링 API)            |
| 남지후 | App – OCR/검색  | Firebase ML Kit, Kakao Local API, 데이터 파싱 & 좌표 변환 |
| 강성경 | App – 지도/경로   | Kakao Map SDK, Mobility API, Polyline 시각화, 지도 UI |
| 이예나 | App – Auth/DB | Firebase Auth, Firestore CRUD, MY 코스 관리 화면       |

---

# 🧪 실행 방법 (Getting Started)

## Android 앱 실행


## 백엔드 실행

```bash
cd python-server
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py
```
---

# 🧩 브랜치 전략 (Branch Strategy)

* `main` – 항상 배포 가능한 안정 버전
* `dev` – 통합 개발 브랜치
* `feature/*` – 기능 단위 브랜치

  * ex) `feature/firebase-auth`, `feature/insta-crawl`, `feature/map-polyline`

---

# 🔥 개발 플로우 (Contribution Guide)

1. Issue 생성 → `feature/기능명` 브랜치 생성
2. 기능 개발
3. PR 생성 (dev로)
4. 팀 리뷰
5. Merge → dev → main (기능 동결 시)

---

# 📌 커밋 규칙 (Commit Convention)

```
feat: 새로운 기능 추가
fix: 버그 수정
chore: 환경설정/빌드/의존성
refactor: 코드 구조 개선
docs: 문서 수정
style: 포맷/세미콜론 등
```

---

# 🎯 프로젝트 목표

* 인스타 장소 탐색 → 지도 코스 생성까지의
  **모든 귀찮은 작업을 자동화**하는 사용자 중심 서비스
* 감성 콘텐츠(SNS) + 실제 동선(지도/경로) 개념을 연결하는
  **새로운 데이트 플래닝 경험** 제공

---
