<script setup>
// SCR-AUTH-001 로그인 / SCR-AUTH-002 회원가입 (라우트로 구분)
import { ref, reactive, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";
import { extractApiError, EMAIL_RE } from "../utils/apiError";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const isSignup = computed(() => route.name === "SCR-AUTH-002");
const form = reactive({ email: "", password: "", name: "" });
const error = ref("");
const notice = ref("");
const noticeType = ref("success");
const loading = ref(false);
const submitted = ref(false);

watch(
  () => [route.name, route.query.signup, route.query.expired].join(),
  () => {
    error.value = "";
    submitted.value = false;
    if (route.query.expired) {
      notice.value = "세션이 만료되었습니다. 다시 로그인해 주세요.";
      noticeType.value = "warning";
    } else if (route.query.signup === "success") {
      notice.value = "회원가입이 완료되었습니다. 로그인해 주세요.";
      noticeType.value = "success";
    } else {
      notice.value = "";
    }
  },
  { immediate: true }
);

const emailError = computed(() => {
  if (!submitted.value || !form.email.trim()) return "";
  return EMAIL_RE.test(form.email.trim()) ? "" : "올바른 이메일 형식이 아닙니다.";
});
const passwordError = computed(() => {
  if (!submitted.value || !form.password) return "";
  if (isSignup.value && form.password.length < 8) return "비밀번호는 8자 이상이어야 합니다.";
  return "";
});
const nameError = computed(() =>
  submitted.value && isSignup.value && !form.name.trim() ? "이름을 입력해 주세요." : ""
);

const isValid = computed(
  () =>
    EMAIL_RE.test(form.email.trim()) &&
    form.password.trim().length >= (isSignup.value ? 8 : 1) &&
    (!isSignup.value || form.name.trim())
);

async function submit() {
  submitted.value = true;
  if (!isValid.value || loading.value) return;
  loading.value = true;
  error.value = "";
  try {
    if (isSignup.value) {
      await auth.signup({ email: form.email, password: form.password, name: form.name });
      router.push({ name: "SCR-AUTH-001", query: { signup: "success" } });
    } else {
      await auth.login({ email: form.email, password: form.password });
      router.push({ name: "SCR-RECIPIENT-001" });
    }
  } catch (e) {
    error.value = extractApiError(e).message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="glow glow--1" />
    <div class="glow glow--2" />

    <div class="card card--pad auth">
      <div class="auth__brand">
        <span class="auth__mark">🎁</span>
        <div>
          <div class="auth__name">이걸주네?</div>
          <div class="auth__tag">AI 맞춤 선물 추천</div>
        </div>
      </div>

      <h1 class="auth__h">{{ isSignup ? "회원가입" : "로그인" }}</h1>
      <p class="auth__sub">
        받는 사람과 상황에 맞는 선물을, 이유와 함께 추천해 드립니다.
      </p>

      <InlineAlert :type="noticeType" :message="notice" />
      <InlineAlert type="error" :message="error" />

      <form @submit.prevent="submit" novalidate>
        <div v-if="isSignup" class="field">
          <label class="field__label">이름</label>
          <input class="input" v-model="form.name" placeholder="이름을 입력하세요" autocomplete="name" />
          <p v-if="nameError" class="form-error">{{ nameError }}</p>
        </div>
        <div class="field">
          <label class="field__label">이메일</label>
          <input class="input" type="email" v-model="form.email" placeholder="you@example.com" autocomplete="email" />
          <p v-if="emailError" class="form-error">{{ emailError }}</p>
        </div>
        <div class="field">
          <label class="field__label">비밀번호</label>
          <input
            class="input"
            type="password"
            v-model="form.password"
            :placeholder="isSignup ? '8자 이상' : '비밀번호'"
            :autocomplete="isSignup ? 'new-password' : 'current-password'"
          />
          <p v-if="passwordError" class="form-error">{{ passwordError }}</p>
        </div>

        <button class="btn btn--primary btn--block btn--lg" :disabled="loading">
          {{ loading ? "처리 중..." : isSignup ? "회원가입" : "로그인" }}
        </button>
      </form>

      <p class="auth__switch">
        <template v-if="isSignup">
          이미 계정이 있으신가요?
          <router-link :to="{ name: 'SCR-AUTH-001' }">로그인</router-link>
        </template>
        <template v-else>
          아직 계정이 없으신가요?
          <router-link :to="{ name: 'SCR-AUTH-002' }">회원가입</router-link>
        </template>
      </p>

      <p class="auth__fine">
        <span class="material-symbols-outlined">verified_user</span>
        로그인 정보는 인증과 개인 데이터 관리에만 사용되며, 다른 가입자를 검색하거나 연결하지 않습니다.
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  position: relative;
  overflow: hidden;
}
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.glow--1 {
  top: -120px;
  right: -80px;
  width: 380px;
  height: 380px;
  background: rgba(255, 122, 0, 0.12);
}
.glow--2 {
  bottom: -100px;
  left: -80px;
  width: 320px;
  height: 320px;
  background: rgba(255, 77, 79, 0.08);
}
.auth {
  width: 440px;
  max-width: 100%;
  padding: 34px;
  position: relative;
  z-index: 1;
}
.auth__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}
.auth__mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: var(--primary-soft);
  display: grid;
  place-items: center;
  font-size: 22px;
  box-shadow: 0 2px 10px rgba(255, 122, 0, 0.22);
}
.auth__name {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.02em;
}
.auth__tag {
  font-family: var(--font-label);
  font-size: 11px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: 0.04em;
}
.auth__h {
  font-size: 22px;
  margin-bottom: 6px;
}
.auth__sub {
  color: var(--text-muted);
  font-size: 13.5px;
  margin-bottom: 22px;
}
.auth__notice {
  background: var(--success-soft);
  color: var(--success);
  border: 1px solid #bfe2cd;
  border-radius: var(--radius);
  padding: 10px 12px;
  font-size: 12.5px;
  margin-bottom: 16px;
}
.auth__switch {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 18px;
}
.auth__switch a {
  font-weight: 700;
}
.auth__fine {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  color: var(--text-faint);
  font-size: 11.5px;
  margin-top: 16px;
  line-height: 1.55;
}
.auth__fine .material-symbols-outlined {
  font-size: 15px;
  color: var(--primary);
  flex-shrink: 0;
}
</style>
