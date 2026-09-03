import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../store/auth";

// 라우트 name은 설계문서 §1.7 화면 ID와 1:1로 맞춥니다 (추적성 유지)
const routes = [
  {
    path: "/",
    name: "SCR-HOME-001",
    component: () => import("../views/HomeView.vue"),
  },
  {
    path: "/login",
    name: "SCR-AUTH-001",
    component: () => import("../views/LoginView.vue"),
  },
  {
    path: "/signup",
    name: "SCR-AUTH-002",
    component: () => import("../views/LoginView.vue"),
  },
  {
    path: "/recipients",
    name: "SCR-RECIPIENT-001",
    component: () => import("../views/RecipientListView.vue"),
  },
  {
    path: "/recipients/:recipientId/previous-gifts",
    name: "SCR-HISTORY-001",
    component: () => import("../views/PreviousGiftsView.vue"),
    props: true,
  },
  {
    path: "/recipients/:recipientId/gift-condition",
    name: "SCR-GIFT-001",
    component: () => import("../views/GiftConditionView.vue"),
    props: true,
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
    path: "/recommendations/:recommendationId",
    name: "SCR-AI-001",
    component: () => import("../views/RecommendationResultView.vue"),
    props: true,
  },
  {
    path: "/recommendations/:recommendationId/re-recommend",
    name: "SCR-AI-002",
    component: () => import("../views/RecommendationFeedbackView.vue"),
    props: true,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 홈·회원가입·로그인만 공개, 그 외는 인증 필요
const PUBLIC_ROUTES = new Set(["SCR-HOME-001", "SCR-AUTH-001", "SCR-AUTH-002"]);
router.beforeEach((to) => {
  const auth = useAuthStore();
  if (!PUBLIC_ROUTES.has(to.name) && !auth.isLoggedIn) {
    return { name: "SCR-AUTH-001" };
  }
  if ((to.name === "SCR-AUTH-001" || to.name === "SCR-AUTH-002") && auth.isLoggedIn) {
    return { name: "SCR-RECIPIENT-001" };
  }
});

export default router;
