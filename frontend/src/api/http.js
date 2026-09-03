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

// 응답 인터셉터: 성공 시 body 반환, 에러는 그대로 전달
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // TODO: 401 처리 -> 토큰 제거 후 로그인 화면(SCR-AUTH-001) 리다이렉트
    return Promise.reject(error);
  }
);

export default http;
