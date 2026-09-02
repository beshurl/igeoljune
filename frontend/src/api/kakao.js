import http from "./http";

// SCR-KAKAO-001 · UC6·UC7 (선택 흐름) 카카오톡 대화 파일 업로드/분석
export function uploadKakaoChat(file) {
  const form = new FormData();
  form.append("file", file);
  return http.post("/kakao-chats", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}

// SCR-KAKAO-002 · UC8 추출 취향 검토/확정
export function fetchExtractedPreferences(chatId) {
  return http.get(`/kakao-chats/${chatId}/preferences`);
}

export function confirmExtractedPreferences(chatId, payload) {
  return http.post(`/kakao-chats/${chatId}/preferences/confirm`, payload);
}
