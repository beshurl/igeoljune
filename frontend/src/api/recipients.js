import http from "./http";

// SCR-RECIPIENT-001 · UC2 추천 대상 관리
export function fetchRecipients() {
  return http.get("/recipients");
}

export function createRecipient(payload) {
  return http.post("/recipients", payload);
}

export function updateRecipient(recipientId, payload) {
  return http.put(`/recipients/${recipientId}`, payload);
}

export function deleteRecipient(recipientId) {
  return http.delete(`/recipients/${recipientId}`);
}
