import { createRouter, createWebHistory } from "vue-router";

// 라우트 name은 화면 정의서의 SCR ID와 1:1로 맞춥니다 (추적성 유지)
const routes = [
  {
    path: "/",
    name: "SCR-AUTH-001",
    component: () => import("../views/LoginView.vue"),
  },
  {
    path: "/recipients",
    name: "SCR-RECIPIENT-001",
    component: () => import("../views/RecipientListView.vue"),
  },
  {
    path: "/calendar",
    name: "SCR-CALENDAR-001",
    component: () => import("../views/CalendarConnectView.vue"),
  },
  {
    path: "/kakao/upload",
    name: "SCR-KAKAO-001",
    component: () => import("../views/KakaoUploadView.vue"),
  },
  {
    path: "/kakao/review",
    name: "SCR-KAKAO-002",
    component: () => import("../views/KakaoReviewView.vue"),
  },
  {
    path: "/recipients/:recipientId/gift-condition",
    name: "SCR-GIFT-001",
    component: () => import("../views/GiftConditionView.vue"),
    props: true,
  },
  {
    path: "/recommendations/:recommendationId",
    name: "SCR-AI-001",
    component: () => import("../views/RecommendationResultView.vue"),
    props: true,
  },
  {
    path: "/recommendations/:recommendationId/feedback",
    name: "SCR-AI-002",
    component: () => import("../views/RecommendationFeedbackView.vue"),
    props: true,
  },
  {
    path: "/history",
    name: "SCR-HISTORY-001",
    component: () => import("../views/HistoryView.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
