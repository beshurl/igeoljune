import axios from "axios";
import mockAdapter from "../mocks/adapter";

// VITE_USE_MOCK 를 "false" 로 명시하지 않으면 목 어댑터를 사용합니다.
// 백엔드 연동 준비가 되면 .env 에 VITE_USE_MOCK=false 를 추가하세요.
const USE_MOCK = import.meta.env.VITE_USE_MOCK !== "false";

// 공통 Axios 인스턴스 — API 명세서 v1: Base URL `/api/v1`
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  withCredentials: true,
  timeout: 15000,
  ...(USE_MOCK ? { adapter: mockAdapter } : {}),
});

// 요청 인터셉터: 인증 토큰 첨부 (회원가입·로그인 제외 전 API 필요)
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터: 성공 시 body 반환, 401 은 세션 만료 처리
let sessionExpiredNotified = false;
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem("accessToken");
      // 라우터/스토어 순환 참조를 피하려고 이벤트로 알림 (App.vue 가 수신)
      if (!sessionExpiredNotified) {
        sessionExpiredNotified = true;
        window.dispatchEvent(new CustomEvent("auth:expired"));
        // 다음 요청을 위해 곧바로 플래그 해제
        setTimeout(() => {
          sessionExpiredNotified = false;
        }, 1000);
      }
    }
    return Promise.reject(error);
  }
);

export default http;
