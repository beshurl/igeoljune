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

> **주의**: 그래서 `./mvnw test` 는 마이그레이션 SQL 을 한 번도 실행하지 않는다.
> `V1`/`V2` 가 깨져 있어도 테스트는 통과한다. 마이그레이션을 추가·수정했다면
> PostgreSQL 로 직접 기동해 확인할 것.

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
| `SERVER_PORT` | 8080 | 애플리케이션 포트 |

Swagger UI: http://localhost:8080/swagger-ui.html

## 패키지 구조

```
com.skala.ikgeoljune
├── common        ErrorCode / ApiException / ErrorResponse / ListResponse / GlobalExceptionHandler
│   └── validation  @NotBlankIfPresent (PATCH 로 빈 문자열이 오는 것을 막는다)
├── config        SecurityConfig, OpenApiConfig, TimeZoneConfig
├── security      JwtTokenProvider, JwtAuthenticationFilter, AuthUser, @CurrentUser, SecurityErrorResponder
├── domain        엔티티 8개 + Enum 5개 (§10)
├── repository    Spring Data JPA
├── dto           요청·응답 record (camelCase)
├── service       도메인별 서비스 + OwnershipValidator + CascadeDeleteService
├── ai            GiftAiClient 인터페이스와 콘텍스트 타입
│   └── mock      MockGiftAiClient, GiftCatalog
└── controller    REST 컨트롤러 (모두 /api/v1)
```

## 계약 문서 대응

구현 기준은 **`DB.dbml`(스키마)** 과 **`API.yml`(REST 계약)** 두 파일이며,
`docs/contract/` 에서 함께 버전 관리한다. 두 파일은 현재 구현과 일치하며,
문서 검증 명령과 dbml 로 표현할 수 없는 제약은 [`docs/contract/README.md`](docs/contract/README.md) 참고.
문서와 코드가 어긋나면 코드가 진실이다. 코드를 문서에 맞추지 말고 문서를 먼저 고친다.

실제 LLM 으로 넘어갈 때의 단계별 계획은 [`docs/roadmap/ai-integration.md`](docs/roadmap/ai-integration.md) 에 있다.

- 스키마: `V1__init.sql` 이 `DB.dbml` 의 테이블·인덱스·CHECK 제약·FK 삭제 규칙을 그대로 옮긴 것
- 오류 코드: `API.yml` 이 정의한 10개(`VALIDATION_ERROR` 400, `UNAUTHORIZED` 401, `RESOURCE_FORBIDDEN` 403,
  `RESOURCE_NOT_FOUND` 404, `METHOD_NOT_ALLOWED` 405, `RESOURCE_CONFLICT` 409, `PAYLOAD_TOO_LARGE` 413,
  `UNSUPPORTED_MEDIA_TYPE` 415, `AI_RESULT_INVALID` 422, `INTERNAL_ERROR` 500)만 쓴다.
  구체적인 상황은 `message` 로 전달하고 `code` 를 세분화하지 않는다.
- 요청 검증: `minProperties: 1`(빈 PATCH 본문 거부), `additionalProperties: false`(모르는 필드 거부)를 실제로 강제한다
- 목록: `{ items, totalCount }`. `GET /recipients` 는 `page`(≥0), `size`(1~100) 를 받는다
- 일시: `Asia/Seoul` 기준 ISO 8601 (`2026-09-03T14:30:00+09:00`)
- 소유권 검증: `OwnershipValidator` 가 candidate → recommendation → condition → recipient → user 경로를 확인한다.
  없으면 404, 남의 리소스면 403

### AI 추천 흐름

MVP 계약은 **동기 처리**다. Mock 추천을 요청 스레드 안에서 실행하고
**201 Created** 로 완성된 후보를 즉시 반환한다. 프론트는 폴링하지 않는다.

```
POST /api/v1/gift-conditions/{conditionId}/recommendations   → 201 { candidates: [...] }
                                                                Location: /api/v1/recommendations/10
GET  /api/v1/recommendations/{recommendationId}              → 200 { candidates: [...] }
```

후보를 만들지 못하면 실행 기록을 남기지 않고 **422 `AI_RESULT_INVALID`** 를 반환한다.

`recommendations.status` / `failure_code` / `failure_message` 컬럼과 `Recommendation.markFailed()` 는
비동기 전환용으로 미리 만들어 두었을 뿐, **동기 처리인 지금은 쓰이지 않는다**(status 는 항상 `SUCCESS`).
전환 시 스키마 변경은 필요 없지만 "실패하면 기록을 남기지 않는다" 정책은 뒤집어야 한다.
자세한 내용은 로드맵 Phase 3 참고.

### 최종 선물 선택

추천 후보 하나를 최종 선물로 선택하면 `selectedAt` 이 기록된다. 한 추천 실행 안에서 최대 1건이다.

```
PUT    /api/v1/recommendation-candidates/{candidateId}/selection   → 200 (후보 객체)
GET    /api/v1/recommendation-candidates/{candidateId}/selection   → 200
DELETE /api/v1/recommendation-candidates/{candidateId}/selection   → 204
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

- 예상 가격은 **카탈로그 실제 가격** 을 그대로 사용한다. 예산으로 보정하지 않는다
- 예산 내 후보를 우선하되 예산 밖 대안도 함께 제안한다(기획의 "예산보다 낮거나 높은 대안")
- `structured_preference` 의 관심사·선호 속성 매칭, `WISH_ITEM` 최우선 가점
- `DISLIKED_CATEGORY` 취향과 `avoidGiftNote` 키워드에 걸리는 상품 제외
- `previous_gifts` 와 같은 상품 제외
- 재추천 시 이전 추천에서 `DISLIKE` 한 상품과 해당 사유의 카테고리 제외

실제 LLM 으로 바꿀 때는 `GiftAiClient` 를 구현한 빈을 추가하고 `AI_PROVIDER` 값만 바꾸면 되며,
서비스·컨트롤러 코드는 건드릴 필요가 없다. `MockGiftAiClient` 는 `matchIfMissing = true` 라
설정을 지우면 다시 mock 으로 돌아온다.

다만 **빈 교체만으로 끝나지는 않는다.** LLM 은 위 제외 규칙을 조용히 어기므로
`Exclusions` 수준의 사후 검증과 프롬프트 인젝션 방어가 함께 필요하다(로드맵 Phase 1~2).

## 현재 구현 범위

`API.yml` 의 엔드포인트는 전부 구현돼 동작하지만, 아래는 **아직 없다**.

| 항목 | 상태 |
|---|---|
| 실제 LLM 연동 | `GiftAiClient` 인터페이스만 있고 구현체는 `MockGiftAiClient` 뿐이다 (카탈로그 30건 + 키워드 규칙) |
| 토큰 갱신·로그아웃 | access token 단일 발급. refresh·무효화·비밀번호 변경 API 없음 |
| 요청 제한 | 로그인·추천 모두 rate limit 없음 |
| 배포 설정 | Dockerfile·CI 워크플로 없음. 로컬 실행 기준이다 |
| 목록 페이지네이션 | `GET /recipients` 에만 있다. 나머지 목록은 전량 반환 |

## 팀 확인이 필요한 지점

1. **Notion 명세서와 `API.yml` 불일치** — 후보 순위 필드가 Notion 은 `recommendation_rank`, `API.yml` 은 `recommendRank` 다.
   구현은 `API.yml`(및 `DB.dbml` 의 `recommend_rank` 컬럼)을 따랐다. Notion 명세서 갱신이 필요하다.
2. **오류 코드 세분화 불가** — `API.yml` 이 `RESOURCE_CONFLICT` 하나로 409 를 표현하므로
   이메일 중복과 취향 중복을 `code` 로 구분할 수 없다. 현재는 `message` 로만 구분한다.
   프론트에서 분기가 필요하면 계약에 코드 추가가 필요하다.
3. **`Idempotency-Key` 헤더 미구현** — `API.yml` 에 "서버 지원 여부를 구현 전에 확정합니다" 로 적혀 있어 보류했다.
   받아만 두고 무시하면 클라이언트가 중복 방지를 신뢰하게 되므로 일부러 구현하지 않았다.
   Mock 에서는 중복 POST 가 추천 레코드만 늘리지만, **실제 LLM 에서는 그대로 비용이라 더 미룰 수 없다**(로드맵 Phase 3).
4. **추천 조건 목록 조회 API 부재** — `API.yml` 에 `GET /recipients/{recipientId}/gift-conditions` 가 없다.
   프론트에서 필요하면 계약에 추가해야 한다.
5. **`relationship` / `ageGroup` / `gender` / `occasionType` / `giftCategory`** — 두 문서 모두 값 목록이 없어 문자열로 두었다.
   Mock 은 카탈로그가 고정이라 문제가 없지만, LLM 은 매번 다른 문자열을 반환할 수 있다.
   **실제 연동 전에 값 목록을 고정해야 한다**(로드맵 Phase 0).
6. **카카오톡 파일 제한** — 5MB / `.txt`, `.csv` 로 잡아 두었다(`app.kakao-analysis.*`).
