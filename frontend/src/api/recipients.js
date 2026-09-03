import http from "./http";

// RECIPIENT-002 목록 조회 — 응답: { items, totalCount }
export function fetchRecipients() {
  return http.get("/recipients");
}

// RECIPIENT-003 상세 조회
export function fetchRecipient(recipientId) {
  return http.get(`/recipients/${recipientId}`);
}

// RECIPIENT-001 등록 — { name, relationship, ageGroup, gender, job }
export function createRecipient(payload) {
  return http.post("/recipients", payload);
}

// RECIPIENT-004 수정 (PATCH · 보낸 필드만)
export function updateRecipient(recipientId, payload) {
  return http.patch(`/recipients/${recipientId}`, payload);
}

// RECIPIENT-005 삭제 (204)
export function deleteRecipient(recipientId) {
  return http.delete(`/recipients/${recipientId}`);
}
