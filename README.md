# 이걸주네? — AI 맞춤형 선물 추천 서비스

SKALA Full-Stack Engineering Mini-project · Vue.js 3 + Spring Boot 3 + PostgreSQL + Spring AI

## 프로젝트 구조

```
ikgeoljune/
  frontend/   Vue 3 (Composition API) + Vite + Pinia + Vue Router + Axios + Bootstrap
  backend/    Spring Boot 3 (Layered Architecture) + PostgreSQL + Spring AI(ChatClient)
  database/   PostgreSQL Docker Compose + 초기 스키마(schema.sql)
  docs/       설계문서·API.yml·DB.dbml·발표자료 등 제출 산출물을 모아두는 폴더
```

각 폴더의 `README.md`에 실행 방법과 세부 구조가 있습니다.

## 빠른 시작 순서

```bash
# 1. Database
cd database && docker compose up -d

# 2. Backend (새 터미널)
cd backend && mvn spring-boot:run

# 3. Frontend (새 터미널)
cd frontend && npm install && npm run dev
```

프론트: http://localhost:5173 · 백엔드: http://localhost:8080/api · Swagger: http://localhost:8080/swagger-ui.html

## 대표 흐름 (채점 기준 — 4개 산출물을 관통하는 하나의 이야기)

**UC7 (조건 입력) → UC8 (AI 추천 요청) → UC9 (결과 확인)** — 예산을 최우선으로 후보와 추천 이유를 생성합니다.

| 계층 | 위치 |
|---|---|
| 화면 | `frontend/src/views/GiftConditionView.vue` → `RecommendationResultView.vue` (SCR-GIFT-001 → SCR-AI-001 ★핵심) |
| API | `backend/.../controller/GiftConditionController`, `RecommendationController` |
| 서비스 / AI 확장 지점 | `backend/.../service/GiftRecommendationService`, `AiGiftAdvisor` |
| DB | `gift_conditions` → `recommendations` → `recommendation_candidates` |

여기에 UC10~UC13(피드백·재추천·확정)이 이어지고, Calendar/카카오톡 연동(UC4~UC6)은 이 흐름에 합류하는 선택 분기입니다. 자세한 매핑은 `frontend/README.md`, `backend/README.md`, `database/README.md`를 참고하세요.

## 화면 정의서 9개 화면 ↔ 코드 위치

| SCR ID | 화면 | FE View | BE Controller |
|---|---|---|---|
| SCR-AUTH-001 | Google 로그인 | LoginView.vue | AuthController |
| SCR-RECIPIENT-001 | 선물 대상 관리 | RecipientListView.vue | RecipientController |
| SCR-CALENDAR-001 (선택) | Calendar 연동 | CalendarConnectView.vue | (TODO) |
| SCR-KAKAO-001 (선택) | 대화 파일 분석 | KakaoUploadView.vue | (TODO) |
| SCR-KAKAO-002 (선택) | 추출 취향 검토 | KakaoReviewView.vue | (TODO) |
| SCR-GIFT-001 | 추천 조건 입력 | GiftConditionView.vue | GiftConditionController |
| SCR-AI-001 ★핵심 | AI 추천 결과 | RecommendationResultView.vue | RecommendationController |
| SCR-AI-002 | 피드백/재추천 | RecommendationFeedbackView.vue | RecommendationController |
| SCR-HISTORY-001 | 선물 이력 | HistoryView.vue | GiftHistoryController |

## 알아둘 점

- 이 환경은 외부 네트워크가 제한되어 있어 `mvn`/`npm`으로 백엔드 의존성 다운로드까지는 여기서 확인했지만
  (프론트는 `npm run build` 성공 확인, DB는 로컬 PostgreSQL로 `schema.sql` 실행 확인),
  백엔드는 실제 인터넷이 되는 팀원 로컬에서 첫 빌드(`mvn clean install`)를 한 번 검증해 주세요.
- 폴더/파일 구조는 팀 컨벤션에 맞게 자유롭게 조정하되, **ID 체계(REQ-F-###, SCR-XXX-###, UC#, snake_case, verb+noun API)** 는
  4개 제출 산출물(설계문서/API.yml/DB.dbml/발표자료)과 반드시 일치시켜 주세요.

## 팀 역할 배분 (다음 단계)

- **BE**: `backend/README.md`의 "다음 할 일" — Google OAuth 실제 연동, JWT, Calendar/카카오톡 API 구현
- **FE**: `frontend/README.md`의 "다음 할 일" — 로딩/에러 UI, 실제 OAuth 연동
- **DA**: `database/README.md`의 "다음 할 일" — DB.dbml 작성, 선택 흐름 테이블 설계
- **API**: `docs/`에 API.yml(OAS) 작성 — 이미 만들어진 컨트롤러의 엔드포인트를 그대로 명세화
- **PM**: 각 파트 진행 상황을 대표 흐름(UC7→UC8→UC9) 기준으로 취합해 설계문서.pdf에 반영
