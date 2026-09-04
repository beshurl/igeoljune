# 계약 문서

구현의 기준이 되는 두 파일을 저장소에서 버전 관리한다.
Notion 문서가 갱신되면 이 파일도 함께 갱신하고, 코드 변경과 같은 PR 에 포함한다.

| 파일 | 역할 |
|---|---|
| `DB.dbml` | PostgreSQL 스키마. `db/migration/V1__init.sql` + `V2__add_candidate_selection.sql` 를 반영 |
| `API.yml` | REST 계약(OpenAPI 3.0.3). 요청·응답 스키마와 오류 코드의 원본 |

두 파일은 2026-09-04 에 **실제 구현(컨트롤러·DTO·마이그레이션)을 기준으로 재작성**했다.
이전 버전은 비동기 추천(202 Accepted) 계약이었고 최종 선물 선택 API 가 빠져 있었다.

앞으로 문서와 코드가 어긋나면 **코드가 진실**이다. 코드를 문서에 맞추지 말고 문서를 먼저 고친다.

## 검증

```bash
# OpenAPI 문법 검증
npx --yes @redocly/cli@latest lint docs/contract/API.yml

# DBML 문법 검증 (PostgreSQL DDL 로 변환)
npx --yes -p @dbml/cli@latest dbml2sql docs/contract/DB.dbml --postgres

# Prism mock 실행
npx --yes @stoplight/prism-cli mock docs/contract/API.yml --host 127.0.0.1 --port 4010 --errors
```

`redocly lint` 는 통과하지만 경고 4건이 남는다.
`RecommendationDetail.failure` 와 `GiftCandidate.feedback` 처럼
`nullable: true` 와 `allOf: [$ref]` 를 함께 쓴 속성의 예시에서 `null` 을 거부하는 것으로,
OpenAPI 3.0 스펙상으로는 올바른 표현이며 린터의 알려진 한계다. 무시해도 된다.

## 두 문서에 담긴 구현 사실 (이전 버전과 달라진 점)

1. **추천은 동기다.** `POST /gift-conditions/{conditionId}/recommendations` 와 재추천은
   Mock AI 를 요청 스레드 안에서 실행하고 후보까지 채운 **`201 Created` + `RecommendationDetail`** 을
   Location 헤더와 함께 반환한다. 프론트는 PROCESSING 을 폴링하지 않는다.
   `recommendations.status` 컬럼과 `RecommendationStatus` 는 실제 LLM 연동 시
   비동기 + 폴링으로 전환할 자리로 남겨 둔 것이며, 실패는 422 로 전체 롤백되므로 응답에는 SUCCESS 만 나온다.
2. **최종 선물 선택 API 3종**이 포함되어 있다.
   `PUT` / `GET` / `DELETE /api/v1/recommendation-candidates/{candidateId}/selection`,
   그리고 `GiftCandidate.selectedAt`.
   UC/REQ 번호는 부여하지 않았다. 계약 문서와 구현으로만 관리한다.
3. **`PreferenceBulkCreateRequest.sourceType`** 은 선택 필드다.
   프론트와 Notion 이 보내므로 받되 KAKAO 만 허용하고, 생략하면 서버가 KAKAO 로 설정한다.
4. **후보의 AI 출력 필드는 nullable 이다.**
   `giftCategory`, `estimatedPriceMin/Max`, `consideredInfo`, `cautionNote` 는 DB 에서도 NULL 을 허용한다.
   항상 채워지는 것은 `giftName`, `recommendationReason`, `recommendRank` 뿐이다.
5. **500 오류 코드는 `INTERNAL_ERROR`** 다 (`INTERNAL_SERVER_ERROR` 아님). `ErrorCode` enum 과 일치시켰다.

## dbml 로 표현할 수 없는 제약

`uq_candidate_selected_per_recommendation` 은 조건부(partial) 유니크 인덱스라 dbml 문법으로 옮길 수 없다.

```sql
CREATE UNIQUE INDEX uq_candidate_selected_per_recommendation
    ON recommendation_candidates (recommendation_id) WHERE selected_at IS NOT NULL;
```

`indexes` 블록에 일반 unique 로 적으면 "추천 실행당 후보 1건" 이라는 **잘못된 제약**이 생성되므로
주석과 테이블 Note 로만 남겼다. `V2__add_candidate_selection.sql` 이 진짜 정의다.

## MVP 범위에서 제외한 기획 항목

1. **피드백 추가 의견(자유 텍스트)** — 프론트는 `dislikeReason` 을 8개 enum 코드로만 전송하며
   자유 입력 UI 가 없다(`RecommendationResultView.vue` 라디오 버튼).
   `dislike_reason` 은 `chk_feedback_dislike_reason` CHECK 제약으로 enum 값만 허용하므로 재사용할 수 없다.
   필요해지면 `feedback.comment TEXT` 컬럼을 새로 추가해야 한다.
2. **추출 취향의 활성 플래그** — 저장된 취향은 항상 추천 콘텍스트에 포함된다.
   사용 중지는 별도 플래그 대신 `PREF-005 DELETE /preferences/{preferenceId}` 삭제로 처리한다.
