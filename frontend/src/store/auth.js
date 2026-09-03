import { defineStore } from "pinia";
import { signup, login, fetchMe } from "../api/auth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    accessToken: localStorage.getItem("accessToken") || null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.accessToken,
  },
  actions: {
    applySession(res) {
      this.accessToken = res.accessToken;
      this.user = res.user;
      localStorage.setItem("accessToken", res.accessToken);
    },
    async login(credentials) {
      this.applySession(await login(credentials));
    },
    // 설계문서 §1.8: 회원가입 성공 → 로그인 화면으로 (자동 로그인 아님)
    async signup(payload) {
      return signup(payload);
    },
    async loadMe() {
      this.user = await fetchMe();
    },
    // 로그아웃 API 는 명세서에 없음 → 클라이언트에서 토큰만 제거
    logout() {
      this.accessToken = null;
      this.user = null;
      localStorage.removeItem("accessToken");
    },
  },
});
