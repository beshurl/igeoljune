<script setup>
// SCR-AUTH-001 · UC1 Google 로그인
import { useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";

const router = useRouter();
const authStore = useAuthStore();

async function handleGoogleLogin() {
  // TODO: Google OAuth 팝업/리다이렉트에서 받은 idToken 전달
  await authStore.loginWithGoogle("dummy-id-token");
  router.push({ name: "SCR-RECIPIENT-001" });
}
</script>

<template>
  <section class="d-flex flex-column align-items-center justify-content-center vh-100">
    <h1 class="mb-2">이걸주네?</h1>
    <p class="text-muted mb-4">AI 맞춤형 선물 추천</p>
    <button class="btn btn-dark d-flex align-items-center gap-2" @click="handleGoogleLogin">
      Google로 계속하기
    </button>
  </section>
</template>
