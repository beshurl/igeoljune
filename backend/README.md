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

## 명세서 대응

- 공통 응답: 단건은 리소스 객체, 목록은 `{ "items": [], "totalCount": 0 }` (`ListResponse`)
- 공통 오류: `{ "code", "message", "fieldErrors" }` (`ErrorResponse`, `GlobalExceptionHandler`)
- 소유권 검증(§1.5): `OwnershipValidator` 가 candidate → recommendation → condition → recipient → user 경로를 확인한다. 없으면 404, 남의 리소스면 403.
- 일시: `Asia/Seoul` 기준 ISO 8601 (`2026-09-03T14:30:00+09:00`)
- 회원가입·로그인을 제외한 모든 API 는 `Authorization: Bearer {accessToken}` 필요

### AI 추천 흐름

`RECOMMEND-001` / `RECOMMEND-004` 는 `recommendations` 레코드를 `PROCESSING` 상태로 만들고 **202 Accepted** 를 즉시 반환한다.
실제 AI 호출은 트랜잭션 커밋 후 `aiTaskExecutor` 스레드에서 실행되고, 끝나면 상태가 `SUCCESS` 또는 `FAILED` 로 바뀐다.
프론트엔드는 `RECOMMEND-002` 를 폴링해서 완료를 확인하면 된다.

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

- 예산 범위(`budgetMin` ~ `budgetMax`) 밖 상품 감점, 예상 가격은 예산 안으로 절삭
- `structured_preference` 의 관심사·선호 속성 매칭, `WISH_ITEM` 최우선 가점
- `DISLIKED_CATEGORY` 취향과 `avoidGiftNote` 키워드에 걸리는 상품 제외
- `previous_gifts` 와 같은 상품 제외
- 재추천 시 이전 추천에서 `DISLIKE` 한 상품과, `TASTE_MISMATCH`·`WANT_DIFFERENT_STYLE` 사유의 카테고리 제외

실제 LLM 으로 바꿀 때는 `GiftAiClient` 를 구현한 빈을 추가하고 `AI_PROVIDER` 값만 바꾸면 되며,
서비스·컨트롤러 코드는 건드릴 필요가 없다.

## 팀 확인이 필요한 지점

명세서를 그대로 따르되, 아래 항목은 판단이 필요해 다음과 같이 구현했다.

1. **`recommendationRank` 필드명** — RECOMMEND-002 예시에는 `recommendation_rank` 로 되어 있지만 §1.1 의 "JSON 필드명은 camelCase" 규칙을 따라 **`recommendationRank`** 로 내보낸다.
2. **`status = FAILED` 응답** — ERD 에 실패 사유 컬럼이 없어, 200 OK 로 `status: "FAILED"` + `candidates: []` 를 반환한다. 별도 오류 응답이 필요하면 `recommendations` 에 `failure_reason` 컬럼 추가가 필요하다.
3. **`relationship` / `ageGroup` / `gender` / `occasionType` / `giftCategory`** — §10 에 값 목록이 없어 문자열로 두었다. 값이 확정되면 Enum 으로 승격한다.
4. **PREF-002 중복 처리** — 이미 있는 `(recipientId, type, value)` 항목은 409 대신 **건너뛰고** 신규만 저장한다(분석 결과 재저장을 막지 않기 위해). PREF-001 단건 등록은 409 를 반환한다.
5. **추천 조건 목록 조회 API 부재** — 명세서에 `GET /recipients/{recipientId}/gift-conditions` 가 없다. 프론트에서 필요하면 추가해야 한다.
6. **카카오톡 파일 제한** — 5MB / `.txt`, `.csv` 로 잡아 두었다(`app.kakao-analysis.*`). 실제 내보내기 파일 형식에 맞춰 조정 필요.
