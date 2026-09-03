# 계약 문서

구현의 기준이 되는 두 파일을 저장소에서 버전 관리한다.
Notion 문서가 갱신되면 이 파일도 함께 갱신하고, 코드 변경과 같은 PR 에 포함한다.

| 파일 | 역할 |
|---|---|
| `DB.dbml` | PostgreSQL 스키마. `src/main/resources/db/migration/V1__init.sql` 의 원본 |
| `API.yml` | REST 계약(OpenAPI 3.0.3). 요청·응답 스키마와 오류 코드의 원본 |

Prism mock 실행:

```bash
npx --yes @stoplight/prism-cli mock docs/contract/API.yml --host 127.0.0.1 --port 4010 --errors
```

## 현재 구현과 어긋나 갱신이 필요한 항목

아래는 리뷰 합의에 따라 **구현이 앞서 나간** 부분이다. `API.yml` 반영이 필요하다.

1. **RECOMMEND-001 / RECOMMEND-004 응답** — `API.yml` 은 `202 Accepted` + `RecommendationAccepted`(status=PROCESSING)
   이지만, MVP 계약은 Mock 을 요청 안에서 실행해 **`201 Created` + 후보 포함(`RecommendationDetail`)** 이다.
   Notion 명세서는 이미 201 로 갱신되어 있다.
2. **PREF-002 `sourceType`** — `API.yml PreferenceBulkCreateRequest` 에는 없지만
   프론트와 Notion 이 `{ "sourceType": "KAKAO", "items": [...] }` 를 보낸다.
   서버는 필드를 받되 KAKAO 만 허용한다.
3. **최종 선물 선택** — `API.yml` 에 엔드포인트가 없다. 구현한 계약은 아래와 같다.
   - `PUT /api/v1/recommendation-candidates/{candidateId}/selection` → 200, 후보 객체(`selectedAt` 포함)
   - `GET /api/v1/recommendation-candidates/{candidateId}/selection` → 200
   - `DELETE /api/v1/recommendation-candidates/{candidateId}/selection` → 204
   - `GiftCandidate` 에 `selectedAt`(nullable) 추가. 한 추천 실행 안에서 최대 1건만 선택된다.
4. **`DB.dbml` `recommendation_candidates.selected_at`** — 3번을 위해 `V2__add_candidate_selection.sql` 로 추가했다.
   dbml 에도 반영이 필요하다.

## MVP 범위에서 제외한 기획 항목

프론트(`igeol-front`) 구현과 대조해 아래 두 항목은 MVP 범위에서 제외했다.
UC/REQ 문서에도 같은 내용을 반영해야 한다.

1. **피드백 추가 의견(자유 텍스트)** — 프론트는 `dislikeReason` 을 8개 enum 코드로만 전송하며
   자유 입력 UI 가 없다(`RecommendationResultView.vue` 라디오 버튼).
   `dislike_reason` 은 `chk_feedback_dislike_reason` CHECK 제약으로 enum 값만 허용하므로 재사용할 수 없다.
   필요해지면 `feedback.comment TEXT` 컬럼을 새로 추가해야 한다.
2. **추출 취향의 지속 사용 여부(활성 플래그)** — 저장된 취향은 항상 추천 콘텍스트에 포함된다.
   사용 중지는 별도 플래그 대신 `PREF-005 DELETE /preferences/{preferenceId}` 삭제로 처리한다.
