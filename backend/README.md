# 이걸주네? Backend

AI 맞춤형 선물 추천 서비스 백엔드. [API 명세서](https://app.notion.com/p/API-3cf79cc0631e80319261ee2251caeb32) v1 기준으로 구현했다.

## 기술 스택

| 항목 | 값 |
|---|---|
| Java | 21 |
| Framework | Spring Boot 3.5.16 (Web, Validation, Security, Data JPA) |
| DB | PostgreSQL (테스트는 H2) |
| 인증 | JWT (jjwt 0.12.6) + BCrypt |
| AI | Mock 구현 (`app.ai.provider=mock`) |
| 문서 | springdoc-openapi (`/swagger-ui.html`) |

## 실행

### 1) 빠른 실행 — PostgreSQL 없이 (권장: FE 연동·기능 확인용)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

인메모리 H2 로 뜬다. 서버를 내리면 데이터가 사라지지만 API 는 전부 동일하게 동작한다.

### 2) PostgreSQL 로 실행

먼저 DB 와 롤을 만들어야 한다. 없으면
`FATAL: password authentication failed for user "ikgeoljune"` 로 부팅이 실패한다.

```bash
psql -d postgres -c "CREATE ROLE ikgeoljune WITH LOGIN PASSWORD 'ikgeoljune';"
psql -d postgres -c "CREATE DATABASE ikgeoljune OWNER ikgeoljune;"
```

> Homebrew 설치본은 로컬 trust 인증이라 현재 계정으로 바로 붙는다.
> EDB 인스톨러판을 쓴다면 `-U postgres -h localhost` 를 붙이고 설치 시 정한 비밀번호를 입력해야 한다.
> 두 개를 같이 깔면 5432 포트가 충돌하니 하나만 띄울 것.

```bash
./mvnw spring-boot:run
```

접속 정보가 다르면 환경변수로 덮어쓴다.

```bash
DB_URL=jdbc:postgresql://localhost:5432/mydb DB_USERNAME=me DB_PASSWORD=secret ./mvnw spring-boot:run
```

### 테스트

```bash
./mvnw test
```

## DB 마이그레이션

스키마는 **Flyway** 가 소유한다 (`src/main/resources/db/migration`).
Hibernate 는 `ddl-auto: validate` 로 검증만 하며 스키마를 바꾸지 않는다.

- 엔티티를 변경했다면 반드시 새 마이그레이션(`V2__*.sql`, `V3__*.sql` ...)을 추가한다.
- `local` 프로필과 테스트는 인메모리 H2 라 Flyway 를 끄고 `create-drop` 을 쓴다.

### 기존 DB 에서 업그레이드

Google OAuth 시절 스키마(`users.google_sub`, `recipients` 등)가 남아 있는 DB 로 기동하면
Flyway 가 아래 메시지와 함께 **실행을 거부한다**. 조용히 깨지지 않게 막아 두었다.

```
Found non-empty schema(s) "public" but no schema history table.
```

이전 스키마에서 자동 업그레이드는 제공하지 않는다.
OAuth 로 가입한 계정에는 `password_hash` 로 채울 값이 없어 backfill 이 불가능하기 때문이다.
아직 배포 전 단계이므로 **DB 재생성이 계약**이다.

```bash
psql -d postgres -c "DROP DATABASE ikgeoljune;"
```

```bash
psql -d postgres -c "CREATE DATABASE ikgeoljune OWNER ikgeoljune;"
```

운영 데이터가 생긴 뒤라면 이 정책을 바꿔야 하며, 그때는 rename/backfill/drop 순서를 보장하는
마이그레이션을 `V2__` 로 추가해야 한다.

기본 접속 정보는 `src/main/resources/application.yml` 에 있고, 모두 환경변수로 덮어쓸 수 있다.

| 환경변수 | 기본값 | 설명 |
|---|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | localhost:5432/ikgeoljune | PostgreSQL 접속 |
| `JWT_SECRET` | 개발용 기본값 | **운영 배포 전 반드시 교체** (HS256, 32byte 이상) |
| `JWT_EXPIRES_IN` | 3600 | access token 만료(초). AUTH-002 의 `expiresIn` 과 같은 값 |
| `CORS_ALLOWED_ORIGINS` | localhost:5173, localhost:3000 | 프론트 개발 서버 |
| `AI_PROVIDER` | mock | AI 구현 선택 |
| `AI_MOCK_LATENCY_MS` | 800 | Mock AI 가 흉내내는 처리 지연 |

Swagger UI: http://localhost:8080/swagger-ui.html

## 패키지 구조

```
com.skala.ikgeoljune
├── common        ErrorCode / ApiException / ErrorResponse / ListResponse / GlobalExceptionHandler
├── config        SecurityConfig, AsyncConfig, OpenApiConfig, TimeZoneConfig
├── security      JwtTokenProvider, JwtAuthenticationFilter, AuthUser, @CurrentUser
├── domain        엔티티 8개 + Enum 5개 (§10)
├── repository    Spring Data JPA
├── dto           요청·응답 record (camelCase)
├── service       도메인별 서비스 + OwnershipValidator + 비동기 추천 처리
├── ai            GiftAiClient 인터페이스와 콘텍스트 타입
│   └── mock      MockGiftAiClient, GiftCatalog
└── controller    REST 컨트롤러 (모두 /api/v1)
```

## 계약 문서 대응

구현 기준은 **`DB.dbml`(스키마)** 과 **`API.yml`(REST 계약)** 두 파일이다.
Notion 명세서와 어긋나는 부분은 이 두 파일을 따른다.

- 스키마: `V1__init.sql` 이 `DB.dbml` 의 테이블·인덱스·CHECK 제약·FK 삭제 규칙을 그대로 옮긴 것
- 오류 코드: `API.yml components/responses` 의 `VALIDATION_ERROR`, `UNAUTHORIZED`, `RESOURCE_FORBIDDEN`,
  `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `AI_RESULT_INVALID` 만 사용한다.
  구체적인 상황은 `message` 로 전달하고 `code` 를 세분화하지 않는다.
- 요청 검증: `minProperties: 1`(빈 PATCH 본문 거부), `additionalProperties: false`(모르는 필드 거부)를 실제로 강제한다
- 목록: `{ items, totalCount }`. `GET /recipients` 는 `page`(≥0), `size`(1~100) 를 받는다
- 일시: `Asia/Seoul` 기준 ISO 8601 (`2026-09-03T14:30:00+09:00`)
- 소유권 검증: `OwnershipValidator` 가 candidate → recommendation → condition → recipient → user 경로를 확인한다.
  없으면 404, 남의 리소스면 403

### AI 추천 흐름

`RECOMMEND-001` / `RECOMMEND-004` 는 `recommendations` 레코드를 `PROCESSING` 상태로 만들고
**202 Accepted** 와 `Location: /api/v1/recommendations/{id}` 헤더를 즉시 반환한다.
실제 AI 호출은 트랜잭션 커밋 후 `aiTaskExecutor` 스레드에서 실행되고, 끝나면 상태가 `SUCCESS` 또는 `FAILED` 로 바뀐다.
FAILED 이면 `failure: { code, message }` 가 함께 내려간다.

```
POST /api/v1/gift-conditions/{conditionId}/recommendations   → 202 { status: "PROCESSING" }
GET  /api/v1/recommendations/{recommendationId}              → 200 { status: "PROCESSING", candidates: [] }
GET  /api/v1/recommendations/{recommendationId}              → 200 { status: "SUCCESS",    candidates: [...] }
```

### Mock AI 교체 방법

AI 연동 경계는 `com.skala.ikgeoljune.ai.GiftAiClient` 하나다.

```java
public interface GiftAiClient {
    List<AiExtractedPreference> extractPreferences(AiKakaoAnalysisContext context);  // KAKAO-001
    List<AiGiftCandidate> recommendGifts(AiRecommendationContext context);           // RECOMMEND-001/004
}
```

`MockGiftAiClient` 는 `@ConditionalOnProperty(app.ai.provider=mock)` 으로 등록되어 있고,
카탈로그 기반 규칙으로 아래를 실제로 반영한다.

- 예산 범위 밖 상품 제외, 예상 가격은 카탈로그 가격대 ∩ 예산
- `structured_preference` 의 관심사·선호 속성 매칭, `WISH_ITEM` 최우선 가점
- `DISLIKED_CATEGORY` 취향과 `avoidGiftNote` 키워드에 걸리는 상품 제외
- `previous_gifts` 와 같은 상품 제외
- 재추천 시 이전 추천에서 `DISLIKE` 한 상품과 해당 사유의 카테고리 제외

실제 LLM 으로 바꿀 때는 `GiftAiClient` 를 구현한 빈을 추가하고 `AI_PROVIDER` 값만 바꾸면 되며,
서비스·컨트롤러 코드는 건드릴 필요가 없다.

## 팀 확인이 필요한 지점

1. **Notion 명세서와 `API.yml` 불일치** — 후보 순위 필드가 Notion 은 `recommendation_rank`, `API.yml` 은 `recommendRank` 다.
   구현은 `API.yml`(및 `DB.dbml` 의 `recommend_rank` 컬럼)을 따랐다. Notion 명세서 갱신이 필요하다.
2. **오류 코드 세분화 불가** — `API.yml` 이 `RESOURCE_CONFLICT` 하나로 409 를 표현하므로
   이메일 중복과 취향 중복을 `code` 로 구분할 수 없다. 현재는 `message` 로만 구분한다.
   프론트에서 분기가 필요하면 계약에 코드 추가가 필요하다.
3. **`Idempotency-Key` 헤더 미구현** — `API.yml` 에 "서버 지원 여부를 구현 전에 확정합니다" 로 적혀 있어 보류했다.
   받아만 두고 무시하면 클라이언트가 중복 방지를 신뢰하게 되므로 일부러 구현하지 않았다.
4. **추천 조건 목록 조회 API 부재** — `API.yml` 에 `GET /recipients/{recipientId}/gift-conditions` 가 없다.
   프론트에서 필요하면 계약에 추가해야 한다.
5. **`relationship` / `ageGroup` / `gender` / `occasionType` / `giftCategory`** — 두 문서 모두 값 목록이 없어 문자열로 두었다.
6. **카카오톡 파일 제한** — 5MB / `.txt`, `.csv` 로 잡아 두었다(`app.kakao-analysis.*`).
