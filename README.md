# 이걸주네? 🎁

> 받는 사람의 취향을 모아 AI가 선물을 골라주는 맞춤형 선물 추천 서비스

선물 고르기는 검색의 문제가 아니라 **종합의 문제**입니다. 관계, 연령대, 취향, 예산, 기념일, 이전에 준 선물, 피하고 싶은 것까지 한 번에 고려해야 하는데 그 정보는 쇼핑몰·SNS·카카오톡·기억에 흩어져 있습니다.

"이걸주네?"는 이 정보를 한곳에 모으고, AI가 조건을 종합해 **선물 후보와 그 추천 이유**를 함께 제시합니다. 마음에 들지 않으면 사유를 남겨 재추천을 받고, 최종 선물을 선택해 기록으로 남깁니다.

---

## 사용 흐름

```
회원가입 · 로그인
      ↓
선물 대상 등록          이름 / 관계 / 연령대 / 성별 / 직업
      ↓
취향 수집               ┌ 카카오톡 대화 업로드 → AI 취향 추출 → 검토 후 승인 항목만 저장
                        └ 직접 입력 (관심사 / 선호 · 비선호 / 희망 품목)
      ↓
과거 선물 기록          중복 추천 방지
      ↓
추천 조건 입력          예산 / 기념일 / 카테고리 / 피하고 싶은 것
      ↓
AI 추천                 후보 목록 + 추천 이유 + 반영 정보 + 주의사항
      ↓
피드백 · 재추천         싫어요 사유를 반영해 다시 추천 (이전 추천과 계보 연결)
      ↓
최종 선물 선택          추천 1건당 1개
```

UC1~UC11과 화면 ID(`SCR-*`), 요구사항 ID(`REQ-F-*`)로 정의된 흐름이며, 설계문서의 UserFlow와 대응합니다.

---

## 시스템 아키텍처

<img width="1626" height="1097" alt="image" src="https://github.com/user-attachments/assets/da3394b4-0d5f-471a-be6f-d3adfe64f9fc" />



프론트엔드는 Vue 3와 Pinia로 화면과 상태를 관리하고 Axios로 백엔드 API를 호출합니다. 인증이 필요한 요청에는 JWT를 포함합니다.

백엔드는 Spring Boot 기반으로 Controller가 HTTP 요청을 받고, Service가 대상 관리·추천·피드백 같은 업무 규칙을 처리하며, Repository가 JPA를 통해 PostgreSQL과 연결합니다.

AI 연동은 `GiftAiClient` 인터페이스로 분리했습니다. 현재는 Mock 구현체가 카탈로그 규칙으로 추천을 만들고, 실제 LLM은 Qwen·Gemma·GPT 계열을 후보로 검토 중입니다. 구현체를 교체해도 서비스와 컨트롤러, 프론트엔드가 받는 응답 구조는 그대로 유지됩니다.

---

## 데이터 모델

<img width="1455" height="811" alt="image" src="https://github.com/user-attachments/assets/af013997-9f12-40ae-b4a0-a5b493ee2a25" />


사용자에서 시작해 추천 대상 → 취향·과거 선물·추천 조건 → 추천 실행 → 후보 → 피드백으로 이어지며, **추천의 근거를 끝까지 추적할 수 있게** 설계했습니다.

| 테이블 | 역할 | 핵심 제약 |
| --- | --- | --- |
| `users` | 회원 인증 · 소유권 루트 | `email` UNIQUE |
| `recipient` | 사용자별 비공개 선물 대상 | FK `user_id` |
| `structured_preference` | 직접 입력 / 카카오톡 승인 취향 | UNIQUE(recipient, type, value) |
| `previous_gifts` | 과거에 준 선물 | FK `recipient_id` |
| `gift_conditions` | 추천 당시의 조건 | budget CHECK |
| `recommendations` | 추천 실행 · 재추천 계보 | self FK `previous_recommendation_id` |
| `recommendation_candidates` | 추천 후보 N개 | UNIQUE(recommendation_id, recommend_rank) |
| `feedback` | 후보별 현재 피드백 | `candidate_id` UNIQUE |

`gift_conditions`를 `recipient`와 분리한 이유는, 같은 사람에게 주는 선물이라도 생일인지 취업 축하인지에 따라 목적과 예산이 달라지기 때문입니다. 조건은 추천 요청 단위로 저장됩니다.

모든 하위 리소스는 상위가 삭제되면 함께 삭제되고(`ON DELETE CASCADE`), 요청마다 `OwnershipValidator`가 후보 → 추천 → 조건 → 대상 → 사용자 경로로 소유권을 확인합니다. 없으면 404, 남의 리소스면 403입니다.

스키마 원본은 [`backend/docs/contract/DB.dbml`](backend/docs/contract/DB.dbml)이며, `V1__init.sql`이 이를 그대로 옮긴 것입니다.

---

## 구성

| 영역 | 스택 | 문서 |
| --- | --- | --- |
| 프론트엔드 | Vue 3 · Vite · Pinia · Vue Router · axios · Bootstrap 5 | [`frontend/README.md`](frontend/README.md) |
| 백엔드 | Java 21 · Spring Boot 3.5 · Spring Security(JWT) · JPA · PostgreSQL · Flyway | [`backend/README.md`](backend/README.md) |
| 계약 문서 | OpenAPI 3.0.3 (`API.yml`) · DBML (`DB.dbml`) | [`backend/docs/contract/README.md`](backend/docs/contract/README.md) |
| 협업 규칙 | 브랜치 · 커밋 · PR 컨벤션 | [`GitConvention.md`](GitConvention.md) |

두 프로젝트 모두 **백엔드/AI 없이도 단독 실행**할 수 있습니다. 프론트엔드는 목 axios 어댑터로 API를 흉내내고, 백엔드는 `MockGiftAiClient`가 카탈로그 규칙으로 추천을 만들어냅니다.

---

## 브랜치 구성

저장소 하나를 브랜치로 나눠 씁니다. 자세한 규칙은 [`GitConvention.md`](GitConvention.md)를 따릅니다.

| 브랜치 | 관리 대상 |
| --- | --- |
| `main` | 프로젝트 안내 문서와 Git 협업 규칙 |
| `igeol-front` | `frontend/` — Vue 프론트엔드 |
| `igeol-back` | `backend/` — Spring Boot 백엔드 |

```bash
git switch igeol-front   # 프론트엔드
git switch igeol-back    # 백엔드
```

작업 브랜치는 `{type}/{scope}/{feature-name}` 형식으로 각 통합 브랜치에서 생성하고, PR로 같은 브랜치에 병합합니다. `igeol-front`와 `igeol-back`은 서로, 그리고 `main`과도 병합하지 않습니다. `main` 직접 push는 금지합니다.

---

## 빠른 시작

### 프론트엔드 (백엔드 없이)

```bash
git switch igeol-front
cd frontend
npm install
npm run dev          # http://localhost:5173
```

`.env`가 없으면 목 API로 동작하므로 전체 화면 흐름을 바로 확인할 수 있습니다.

### 백엔드 (PostgreSQL 없이)

```bash
git switch igeol-back
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local    # 인메모리 H2
```

- Swagger UI — http://localhost:8080/swagger-ui.html
- 테스트 — `./mvnw test`

### 함께 띄우기

백엔드를 먼저 실행한 뒤, 프론트엔드에서 `.env`를 만들고 목을 끕니다.

```bash
cp .env.example .env
# VITE_USE_MOCK=false
# VITE_API_BASE_URL=http://localhost:8080/api/v1
```

PostgreSQL 실행, 환경변수, Flyway 마이그레이션 정책은 [`backend/README.md`](backend/README.md)에 정리돼 있습니다.

---

## API 개요

Base URL은 `/api/v1`, 인증은 `Authorization: Bearer <JWT>`입니다. 화면이 아니라 **리소스의 책임**을 기준으로 나눴습니다.

| 영역 | 엔드포인트 |
| --- | --- |
| 인증 | `POST /auth/signup` · `POST /auth/login` |
| 사용자 | `GET·PATCH·DELETE /users/me` |
| 선물 대상 | `POST·GET /recipients` · `GET·PATCH·DELETE /recipients/{id}` |
| 취향 | `POST·GET /recipients/{id}/preferences` · `POST .../preferences/bulk` · `PATCH·DELETE /preferences/{id}` |
| 카카오톡 분석 | `POST /recipients/{id}/kakao-analysis` (multipart) |
| 과거 선물 | `POST·GET /recipients/{id}/previous-gifts` · `PATCH·DELETE /previous-gifts/{id}` |
| 추천 조건 | `POST /recipients/{id}/gift-conditions` · `GET·PATCH·DELETE /gift-conditions/{id}` |
| 추천 | `POST·GET /gift-conditions/{id}/recommendations` · `GET /recommendations/{id}` · `POST /recommendations/{id}/re-recommend` |
| 피드백 | `PUT·GET·DELETE /recommendation-candidates/{id}/feedback` |
| 최종 선택 | `PUT·GET·DELETE /recommendation-candidates/{id}/selection` |

**응답 규약**

- 목록 — `{ items: [...], totalCount: n }`
- 오류 — `{ code, message, fieldErrors: [{ field, reason }] }`
- 오류 코드 — `VALIDATION_ERROR` · `UNAUTHORIZED` · `RESOURCE_FORBIDDEN` · `RESOURCE_NOT_FOUND` · `RESOURCE_CONFLICT` · `AI_RESULT_INVALID`
- 일시 — `Asia/Seoul` 기준 ISO 8601 (`2026-09-05T14:30:00+09:00`)
- 추천 요청은 동기 처리라 `201 Created` 응답에 후보가 모두 담겨 옵니다. 프론트는 폴링하지 않습니다.
- 후보를 만들지 못하면 저장을 롤백하고 `422 AI_RESULT_INVALID`로 즉시 반환합니다.

---

## AI 연동

AI는 **두 지점에만** 개입합니다. 카카오톡 대화에서 취향 후보를 추출하는 일과, 조건을 종합해 선물 후보를 만드는 일입니다. 소유권 확인, 필수값·예산 범위 검증, 명시적 제외 조건과 과거 선물 중복 검사는 모두 서버가 담당합니다. 추출된 취향도 바로 저장하지 않고 **사용자가 검토·승인한 항목만** 저장합니다.

AI 응답은 자유 문장이 아니라 검증 가능한 JSON으로 받습니다. 이 필드명은 API 응답, DB 컬럼과 그대로 대응합니다.

```json
{
  "giftName": "커피 드립 세트",
  "giftCategory": "HOME_CAFE",
  "estimatedPriceMin": 40000,
  "estimatedPriceMax": 60000,
  "recommendationReason": "홈카페 취향과 생일 상황을 함께 고려했습니다.",
  "consideredInfo": "홈카페, 생일, 3~7만 원 예산",
  "cautionNote": "보유 중인 드립 도구가 있는지 확인해 주세요.",
  "recommendRank": 1
}
```

AI 경계는 인터페이스 하나입니다.

```java
public interface GiftAiClient {
    List<AiExtractedPreference> extractPreferences(AiKakaoAnalysisContext context);
    List<AiGiftCandidate> recommendGifts(AiRecommendationContext context);
}
```

현재는 `MockGiftAiClient`가 카탈로그 기반 규칙으로 동작합니다. 희망 품목 최우선 가점, 비선호 카테고리·과거 선물·이전 재추천에서 싫어요 한 항목 제외, 예산 내 후보 우선에 예산 밖 대안 병행 같은 기획 요구사항을 실제로 반영합니다.

실제 LLM으로 바꿀 때는 `GiftAiClient` 구현 빈을 추가하고 `AI_PROVIDER` 값만 바꾸면 됩니다.

---

## 팀

3조 · 광주2

| 이름 | 역할 | 담당 |
| --- | --- | --- |
| 문영진 | PM | 전체 일정 및 작업 조율, 발표자료 제작, 발표 |
| 박이완 | Product / UX Designer | Use Case 정의와 사용자 흐름 설계, 와이어프레임 및 디자인 구성 |
| 정준용 | DevOps & Integration | README, Git 규칙, 브랜치 운영, 코드 리뷰, 파트 보조 |
| 김아영 | DA / API Architect | DB 데이터 모델링(정규화), API 규격 작성 |
| 정인제 | Frontend | FE 개발 환경 구축, 핵심 화면 UI 구성 |
| 김단빈 | Backend | BE 개발 환경 구축 및 DB 연동, API 서비스 구현 |

각자 자기 영역만 구현하지 않고, Use Case와 화면·API·데이터베이스가 서로 맞는지 함께 확인하며 진행했습니다.

---

## 의도적으로 구현하지 않은 것

**`Idempotency-Key` 헤더** — `API.yml`에 선택 항목으로 정의돼 있지만 "서버 지원 여부를 구현 전에 확정한다"는 단서가 붙어 있어 구현하지 않았습니다. 헤더를 받아만 두고 무시하면 클라이언트가 중복 방지를 신뢰하게 되므로, 계약이 확정될 때까지 지원하지 않는 편이 안전하다고 판단했습니다.
