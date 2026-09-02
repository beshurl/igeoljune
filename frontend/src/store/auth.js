import { defineStore } from "pinia";
import { loginWithGoogle, logout, fetchMe } from "../api/auth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    accessToken: localStorage.getItem("accessToken") || null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.accessToken,
  },
  actions: {
    async loginWithGoogle(idToken) {
      const res = await loginWithGoogle(idToken);
      this.accessToken = res.accessToken;
      this.user = res.user;
      localStorage.setItem("accessToken", res.accessToken);
    },
    async loadMe() {
      this.user = await fetchMe();
    },
    async logout() {
      await logout();
      this.accessToken = null;
      this.user = null;
      localStorage.removeItem("accessToken");
    },
  },
});
