<script setup>
import { onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "./store/auth";

const router = useRouter();
const auth = useAuthStore();

// http.js 의 401 인터셉터가 발행하는 세션 만료 이벤트 처리
function onSessionExpired() {
  auth.logout();
  if (router.currentRoute.value.name !== "SCR-AUTH-001") {
    router.push({ name: "SCR-AUTH-001", query: { expired: "1" } });
  }
}

onMounted(() => window.addEventListener("auth:expired", onSessionExpired));
onBeforeUnmount(() => window.removeEventListener("auth:expired", onSessionExpired));
</script>

<template>
  <router-view />
</template>
