# 이걸주네? — Frontend

Vue 3 (Composition API) + Vite + Pinia + Vue Router + Axios + Bootstrap

## 실행

```bash
npm install
cp .env.example .env   # 필요 시 API 주소 수정
npm run dev
```

## 폴더 구조

```
src/
  api/        axios 서비스 모듈 (백엔드 REST 호출, endpoint 1:1 대응)
  router/     라우트 정의 — name을 화면 정의서 SCR ID와 동일하게 유지
  store/      Pinia 스토어 (auth, recipient, gift)
  views/      화면 정의서의 9개 화면(SCR-*)에 대응하는 페이지 컴포넌트
  components/ 여러 화면에서 재사용하는 공통 컴포넌트
```

## 화면(View) ↔ SCR ID ↔ Use-Case 매핑

| View 파일 | SCR ID | UC |
|---|---|---|
| LoginView.vue | SCR-AUTH-001 | UC1 |
| RecipientListView.vue | SCR-RECIPIENT-001 | UC2 |
| CalendarConnectView.vue (선택) | SCR-CALENDAR-001 | UC4·UC5 |
| KakaoUploadView.vue (선택) | SCR-KAKAO-001 | UC6·UC7 |
| KakaoReviewView.vue (선택) | SCR-KAKAO-002 | UC8 |
| GiftConditionView.vue | SCR-GIFT-001 | UC3 (대표 흐름 UC7) |
| RecommendationResultView.vue ★핵심 | SCR-AI-001 | UC9·UC10 |
| RecommendationFeedbackView.vue | SCR-AI-002 | UC11·UC12 |
| HistoryView.vue | SCR-HISTORY-001 | UC13 |

## 다음 할 일 (팀원 배분용)

- [ ] Google OAuth 실제 연동 (`src/api/auth.js`)
- [ ] 각 View의 로딩/에러 상태 UI
- [ ] `.env`에 실제 BE 배포 주소 반영
- [ ] Bootstrap 대신 팀 디자인 시스템으로 스타일 다듬기 (선택)
