<script setup>
// 전 화면 공통 상단 네비게이션 (로그인 상태에 따라 우측만 달라짐)
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const userName = computed(() => auth.user?.name || "회원");
const onRecipients = computed(() => route.name === "SCR-RECIPIENT-001");

function goHome() {
  if (route.name !== "SCR-HOME-001") router.push({ name: "SCR-HOME-001" });
}
function goRecipients() {
  if (route.name !== "SCR-RECIPIENT-001") router.push({ name: "SCR-RECIPIENT-001" });
}
function logout() {
  auth.logout();
  router.push({ name: "SCR-HOME-001" });
}
</script>

<template>
  <header class="nav">
    <div class="nav__inner">
      <button class="brand" @click="goHome" aria-label="이걸주네? 홈">
        <img class="brand__logo" src="/logo.png" alt="이걸주네? · AI 맞춤 선물 추천" />
      </button>

      <template v-if="auth.isLoggedIn">
        <nav class="nav__links">
          <button class="nav__link" :class="{ 'nav__link--on': onRecipients }" @click="goRecipients">
            선물 대상 관리
          </button>
        </nav>
        <div class="nav__user" @click="logout" title="로그아웃">
          <span class="nav__avatar">{{ userName.slice(0, 1) }}</span>
          <span class="nav__uname">{{ userName }}님</span>
          <span class="material-symbols-outlined">logout</span>
        </div>
      </template>

      <div class="nav__auth" v-else>
        <button class="btn btn--outline btn--sm" @click="router.push({ name: 'SCR-AUTH-001' })">로그인</button>
        <button class="btn btn--primary btn--sm" @click="router.push({ name: 'SCR-AUTH-002' })">회원가입</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 250, 247, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border);
}
.nav__inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 32px;
}
.brand {
  display: flex;
  align-items: center;
  background: none;
  border: 0;
  cursor: pointer;
  padding: 0;
}
.brand__logo {
  height: 32px;
  width: auto;
  display: block;
}
.nav__links {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav__link {
  font-family: var(--font-label);
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-muted);
  background: none;
  border: 0;
  padding: 8px 16px;
  border-radius: 999px;
  cursor: pointer;
}
.nav__link:hover {
  color: var(--text);
  background: var(--primary-soft);
}
.nav__link--on {
  color: var(--primary);
  background: var(--primary-soft);
}
.nav__user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-left: auto;
}
.nav__user .material-symbols-outlined {
  font-size: 17px;
  color: var(--text-faint);
}
.nav__avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 0 0 2px var(--primary-soft);
}
.nav__uname {
  font-family: var(--font-label);
  font-size: 12.5px;
  font-weight: 600;
}
.nav__auth {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

@media (max-width: 640px) {
  .nav__inner {
    gap: 12px;
    padding: 0 16px;
  }
  .nav__uname {
    display: none;
  }
  .brand__logo {
    height: 26px;
  }
  .nav__link {
    padding: 8px 10px;
    font-size: 12.5px;
  }
}
</style>
