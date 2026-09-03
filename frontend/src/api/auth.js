import http from "./http";

// SCR-AUTH-001 · UC1 Google 로그인
export function loginWithGoogle(idToken) {
  return http.post("/auth/google-login", { idToken });
}

export function logout() {
  return http.post("/auth/logout");
}

export function fetchMe() {
  return http.get("/auth/me");
}
