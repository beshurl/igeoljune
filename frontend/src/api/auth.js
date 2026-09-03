import http from "./http";

// AUTH-001 회원가입 — 응답: { userId, email, name, createdAt } (토큰 없음)
export function signup({ email, password, name }) {
  return http.post("/auth/signup", { email, password, name });
}

// AUTH-002 로그인 — 응답: { accessToken, tokenType, expiresIn, user }
export function login({ email, password }) {
  return http.post("/auth/login", { email, password });
}

// USER-001 내 정보 조회
export function fetchMe() {
  return http.get("/users/me");
}

// USER-002 내 이름 수정
export function updateMe({ name }) {
  return http.patch("/users/me", { name });
}
