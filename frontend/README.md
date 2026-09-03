# 이걸주네? — Frontend

AI 맞춤형 선물 추천 서비스의 프론트엔드 (Vue 3 + Vite).

## 실행

```bash
npm install
npm run dev        # http://localhost:5173
npm run build      # dist/ 정적 빌드
npm run preview     # 빌드 결과 로컬 확인
```

## 환경 변수

`.env.example` 참고. **파일이 없으면 목(mock) API 로 동작**하므로 백엔드 없이 전체 흐름을 확인할 수 있다.

| 키 | 기본값 | 설명 |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | 백엔드 Base URL (API 명세서 v1) |
| `VITE_USE_MOCK` | (미설정 = 목 사용) | `false` 로 두면 실제 백엔드 호출 |

실제 백엔드 연동 시:
```bash
cp .env.example .env   # 그리고 VITE_USE_MOCK=false, VITE_API_BASE_URL 설정
```

## 화면 ↔ 라우트 ↔ SCR ID

| SCR ID | 화면 | 경로 | 인증 |
|---|---|---|---|
| SCR-HOME-001 | 서비스 소개 (랜딩) | `/` | 공개 |
| SCR-AUTH-001 | 로그인 | `/login` | 공개 |
| SCR-AUTH-002 | 회원가입 | `/signup` | 공개 |
| SCR-RECIPIENT-001 | 선물 대상 관리 | `/recipients` | 필요 |
| SCR-HISTORY-001 | 과거 선물 관리 | `/recipients/:recipientId/previous-gifts` | 필요 |
| SCR-GIFT-001 | 추천 조건 입력 | `/recipients/:recipientId/gift-condition` | 필요 |
| SCR-KAKAO-001 | 카카오톡 대화 파일 분석 | `/kakao/upload` | 필요 |
| SCR-KAKAO-002 | 추출 취향 검토 | `/kakao/review` | 필요 |
| SCR-AI-001 | 추천 결과 | `/recommendations/:recommendationId` | 필요 |
| SCR-AI-002 | 재추천 진행 | `/recommendations/:recommendationId/re-recommend` | 필요 |

## 구조

```
src/
  api/          axios 래퍼 (http.js) + 도메인별 함수
  store/        Pinia (auth / recipient / gift / kakao)
  mocks/        adapter.js — 백엔드 없이 API 명세 v1 을 흉내내는 axios 어댑터
  constants/    enums.js — relationship/ageGroup/gender/occasion/preferenceType/dislikeReason
  components/   AppNav, InlineAlert
  utils/        apiError.js — axios 에러 → { message, fieldErrors }
  views/        화면 (SCR ID 와 1:1)
```

- 토큰은 `localStorage.accessToken` 에 저장. 요청 인터셉터가 `Authorization: Bearer` 첨부.
- 401 응답 → 토큰 제거 후 `auth:expired` 이벤트 → `App.vue` 가 로그인 화면으로 리다이렉트.
- 목 어댑터도 Bearer 토큰을 확인하고, 예산 역전(400)·중복(409)·미존재(404) 등 오류 경로를 재현한다.

## 백엔드 연동 전 체크리스트

실제 API 붙이기 전에 백엔드 응답이 아래와 일치하는지 확인:

- Base URL `/api/v1`, 인증 헤더 `Authorization: Bearer <JWT>`
- 목록 응답 `{ items: [], totalCount: n }`
- 오류 응답 `{ code, message, fieldErrors: [{ field, reason }] }`
- 회원가입 응답에 **토큰 없음** → 가입 후 로그인 화면으로 이동
- 추천 요청은 처리 중 단계 없이 `201` 응답에 후보 전체가 즉시 온다 (AI 를 Mock 으로 대체). `GET /recommendations/{id}` 는 재진입·새로고침용
- 미지원 (프론트 화면 범위 밖 — 화면 정의 §1.7 에 없음):
  - `USER-003` 회원 탈퇴 (`DELETE /users/me`) — 마이페이지 화면 없음
  - `CONDITION-003` / `CONDITION-004` 추천 조건 수정·삭제 — 조건은 SCR-GIFT-001 에서 생성만 함
- 후보 순위 필드명 = **`recommendRank`** (API.yml 기준)
- enum 값: `constants/enums.js` 와 백엔드 도메인 enum 이 동일해야 함
  - `relationship` / `ageGroup` / `gender` 는 자유 문자열 — **실제 허용 값 목록을 팀에서 확정** (`COWORKER` vs `COLLEAGUE` 등)
- CORS: 개발 시 백엔드가 `http://localhost:5173` origin 허용
