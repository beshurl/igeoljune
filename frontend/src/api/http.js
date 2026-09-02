import axios from "axios";

// 공통 Axios 인스턴스
// BE와의 통신은 항상 이 인스턴스를 통해서만 수행합니다.
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  withCredentials: true,
  timeout: 15000,
});

// 요청 인터셉터: 인증 토큰 첨부 (SCR-AUTH-001 로그인 이후)
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터: 공통 에러 처리
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // TODO: 401 처리 -> 로그인 화면(SCR-AUTH-001) 리다이렉트 등 공통 에러 처리
    return Promise.reject(error);
  }
);

export default http;
