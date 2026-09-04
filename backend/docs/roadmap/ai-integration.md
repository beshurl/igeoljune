# 실제 LLM 연동 로드맵

Mock(`MockGiftAiClient`)에서 실제 LLM 으로 넘어갈 때의 단계별 계획이다.
각 단계는 **그 단계만 끝내도 배포 가능한 상태**가 되도록 잘랐다.

전제: 연동 경계는 `com.skala.ikgeoljune.ai.GiftAiClient` 하나이고,
`app.ai.provider` 로 구현체를 갈아끼운다. 서비스·컨트롤러는 Phase 3 전까지 손대지 않는다.

---

## Phase 0 — 착수 전 확정 (코드 변경 거의 없음)

LLM 을 붙이기 전에 팀이 답을 정해야 하는 항목이다. 여기가 막히면 뒤가 전부 막힌다.

| 항목 | 결정해야 할 것 | 막히면 생기는 일 |
|---|---|---|
| **개인정보** | 카카오톡 대화 원문을 외부 LLM 에 보내도 되는가. 대화 상대방 동의는 어떻게 받는가 | 서비스 자체가 불가. **가장 먼저 답이 나와야 한다** |
| 모델·비용 | 어떤 모델을 쓰고, 추천 1건당 허용 비용/지연은 얼마인가 | Phase 3 비동기 전환 여부의 판단 근거가 없음 |
| 상품 데이터 | LLM 이 상품명을 지어내게 둘 것인가, 실제 카탈로그에 grounding 할 것인가 | 존재하지 않는 상품·틀린 가격을 추천 (Phase 5 참고) |
| 카테고리 값 | `giftCategory` / `relationship` / `occasionType` 을 enum 으로 고정할 것인가 | LLM 이 매번 다른 문자열을 뱉어 통계·필터가 불가능 |

> `giftCategory` 는 현재 `DB.dbml` / `API.yml` 모두 값 목록이 없어 자유 문자열이다.
> LLM 을 붙이는 순간 이 자유도가 그대로 데이터 오염이 된다. **Phase 1 전에 값 목록을 고정할 것.**

---

## Phase 1 — LLM 클라이언트 구현 (동기 유지)

기존 계약(201 Created + 후보 즉시 반환)을 **그대로 두고** 구현체만 바꾼다.
프론트 수정이 0 이라 가장 먼저, 가장 안전하게 넣을 수 있다.

### 할 일

1. `com.skala.ikgeoljune.ai.llm.LlmGiftAiClient implements GiftAiClient`
   - `@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude")`
   - `MockGiftAiClient` 의 `matchIfMissing = true` 는 유지 → 기본값은 계속 mock
2. **structured output** 으로 응답을 강제한다. 자유 텍스트 파싱은 하지 않는다.
   - 출력 스키마를 `AiGiftCandidate` / `AiExtractedPreference` 필드에 1:1 로 맞춘다
   - 파싱 실패·필드 누락 → `AiException` → 서비스가 이미 422 `AI_RESULT_INVALID` 로 변환한다
3. 타임아웃·재시도·폴백
   - 호출 타임아웃(예: 20s), 재시도 1회, 그래도 실패하면 `AiException`
   - `app.ai.fallback-to-mock=true` 일 때 mock 으로 폴백 (데모·장애 대비)
4. 프롬프트를 `resources/prompts/*.md` 로 분리하고 버전 문자열을 로그에 남긴다

### 건드리지 않는 것

`RecommendationService` / `KakaoAnalysisService` / 컨트롤러 / DTO / 스키마.
**이 단계에서 이 파일들이 바뀐다면 경계 설계가 잘못된 것이므로 멈추고 다시 볼 것.**

### 완료 기준

- `AI_PROVIDER=claude ./mvnw spring-boot:run` 으로 뜨고, 기존 통합 테스트가 mock 프로필에서 그대로 통과
- LLM 응답을 stub 한 테스트로 `LlmGiftAiClient` 단위 검증 (정상 / 스키마 위반 / 타임아웃 3케이스)

---

## Phase 2 — 검증·안전장치 (LLM 출력을 믿지 않는 레이어)

LLM 은 제약을 조용히 어긴다. Mock 은 코드로 보장하던 것들을 **사후 검증으로 다시 세운다.**

### 2-1. 출력 검증

`LlmGiftAiClient` 안에서 후보 리스트를 반환하기 전에 확인한다.

- `avoidGiftNote` 와 `DISLIKED_CATEGORY` 취향에 걸리는 후보 제거
- `previous_gifts` 와 중복되는 후보 제거
- 재추천 시 이전 `DISLIKE` 후보 제거
- 가격이 정수이고 `priceMin <= priceMax` 인지
- `giftCategory` 가 Phase 0 에서 고정한 값 목록 안에 있는지

> 이 규칙들은 이미 `MockGiftAiClient.Exclusions` 에 구현돼 있다.
> **`Exclusions` 를 `ai.mock` 에서 `ai` 패키지로 올려 두 구현이 공유하게 한다.**

거른 뒤 후보가 `candidate-count` 에 못 미치면 1회 재요청, 그래도 0 건이면 `AiException`.

### 2-2. 프롬프트 인젝션 방어

카카오톡 대화 **원문이 그대로 프롬프트에 들어간다.** 대화 안에 "이전 지시를 무시하고…"
같은 문장이 있으면 그대로 먹힌다. 대화를 지시가 아닌 **데이터**로 명확히 감싸고,
추출 결과가 스키마를 벗어나면 버린다.

### 2-3. PII 마스킹

`KakaoAnalysisService.readText()` 와 LLM 호출 사이에 마스킹 단계를 넣는다.
전화번호·계좌·주소·이메일은 취향 추출에 필요 없다.

### 완료 기준

- "예산 밖 상품만 반환", "제외 카테고리 포함", "카테고리 오타" 를 stub 으로 주입한 테스트가 전부 걸러짐을 검증
- 대화 원문에 지시문을 넣은 인젝션 테스트 케이스 통과

---

## Phase 3 — 비동기 전환 (계약 변경, 프론트 동반 작업)

Phase 1~2 를 운영해 보고 **응답 시간이 실제로 문제일 때만** 착수한다.
비용이 큰 단계이므로 "느릴 것 같아서" 가 아니라 측정값으로 판단한다.

### 트리거

p95 추천 응답이 5초를 넘거나, 게이트웨이 타임아웃이 관측될 때.

### 할 일

1. **계약 변경**: `POST .../recommendations` → `202 Accepted` + `{ recommendationId, status: "PROCESSING" }`
   - 프론트는 `GET /recommendations/{id}` 를 폴링
   - `API.yml` 은 **원래 202 로 적혀 있었다.** 즉 이 단계는 계약을 되돌리는 것에 가깝다
   - Notion 명세서 · `docs/contract/API.yml` · 프론트 3곳을 같은 PR 에서 갱신
2. **이미 준비된 스키마를 쓴다.** 새 컬럼이 필요 없다.
   - `recommendations.status` (`PROCESSING` / `SUCCESS` / `FAILED`)
   - `failure_code` / `failure_message` — `Recommendation.markFailed()` 가 이미 있고 지금 호출되지 않는다
   - `chk_recommendation_failure_state` CHECK 제약이 상태 정합성을 DB 레벨에서 보장한다
3. **실패 정책이 바뀐다.** 현재는 실패 시 레코드를 남기지 않고 422 를 던진다
   ([RecommendationService.execute](../../src/main/java/com/skala/ikgeoljune/service/RecommendationService.java)).
   비동기에서는 202 를 이미 응답했으므로 **레코드를 `FAILED` 로 남겨야 한다.**
   AI 호출을 별도 트랜잭션으로 분리해, 실패해도 `Recommendation` 저장은 롤백되지 않게 한다.
4. `@Async` 실행기 설정 (스레드 풀 크기 = 동시 LLM 호출 상한)
5. **`Idempotency-Key` 구현.** 여기서부터 POST 중복 = 실제 비용이다. 더 이상 보류할 수 없다
   (README "팀 확인이 필요한 지점" 3번).

### 완료 기준

- 추천 요청 → PROCESSING 조회 → SUCCESS 조회 흐름의 통합 테스트
- LLM 실패 시 `FAILED` + `failure_code` 가 조회되는 테스트
- 같은 `Idempotency-Key` 로 두 번 POST 하면 추천이 1건만 생성됨

---

## Phase 4 — 비용·품질 운영

1. **사용자별 rate limit** — 추천 요청은 곧 돈이다. 지금은 제한이 없다
2. **토큰 사용량 로깅** — 추천 1건당 입력/출력 토큰과 비용을 남긴다
3. **재추천 이력 누적** — 현재 `buildContext` 는 **직전 추천 1건의 DISLIKE 만** 본다.
   `previous_recommendation_id` 체인을 거슬러 올라가 전체 비선호 이력을 콘텍스트에 넣는다.
   (3번째 재추천에서 1번째에 싫다고 한 상품이 다시 나오는 문제)
4. **골든 케이스 평가 세트** — 프롬프트를 고칠 때마다 추천 품질이 나빠지지 않았는지 확인할
   고정 입력 세트를 만든다. 프롬프트 수정의 안전망이다
5. **응답 캐싱** — 같은 조건·같은 취향이면 짧은 TTL 로 재사용

---

## Phase 5 — 상품 데이터 실연동

여기까지 오면 추천 문구는 좋은데 **"그래서 어디서 사?"** 가 남는다.

- LLM 이 상품명을 생성 → 커머스 검색 API 로 실제 상품에 매핑(grounding)
- 매핑된 실제 가격으로 `estimatedPriceMin/Max` 를 채운다 (LLM 이 지어낸 가격 폐기)
- `recommendation_candidates` 에 구매 링크 컬럼 추가 (`V3__*.sql`)
- 매핑 실패한 후보는 링크 없이 내보낼지, 후보에서 제외할지 정책 결정

---

## 병행해야 하는 선행 정비

로드맵과 별개로, **Phase 1 이전에** 해두지 않으면 뒤에서 비용이 커지는 것들.

| 항목 | 이유 |
|---|---|
| CI 파이프라인 (`./mvnw test`) | Phase 2 의 검증 테스트가 자동 실행되지 않으면 의미가 없다 |
| 테스트에서 Flyway 활성화 | 현재 테스트는 H2 + `create-drop` 이라 `V1`/`V2` 마이그레이션이 **한 번도 실행되지 않는다.** Phase 5 의 `V3` 를 안전하게 추가하려면 필수 |
| `JWT_SECRET` 등 운영 설정 분리 | LLM API 키가 추가되면 비밀 관리가 더 미룰 수 없는 문제가 된다 |
| `show-sql: false` (운영 프로필) | LLM 연동 시 로그량이 늘어난다 |

---

## 요약

```
Phase 0  결정      개인정보 · 모델/비용 · 상품 데이터 · 카테고리 값 고정
   ↓
Phase 1  구현      LlmGiftAiClient (동기 유지, 프론트 변경 0)
   ↓
Phase 2  방어      출력 검증 · 인젝션 방어 · PII 마스킹
   ↓             ← 여기까지가 "실제 AI 로 동작하는 서비스"의 최소선
Phase 3  전환      비동기 + 폴링 + Idempotency-Key   (측정값이 요구할 때만)
   ↓
Phase 4  운영      rate limit · 비용 로깅 · 이력 누적 · 평가 세트
   ↓
Phase 5  완결      실제 상품 매핑 + 구매 링크
```

**Phase 2 까지가 데모 가능한 최소선이고, Phase 5 까지가 사용자가 실제로 선물을 살 수 있는 서비스다.**
