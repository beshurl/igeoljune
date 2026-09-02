import http from "./http";

// SCR-HISTORY-001 · UC13·UC14 대상별 추천/선물 이력 조회
export function fetchGiftHistory(recipientId) {
  const params = recipientId ? { recipientId } : {};
  return http.get("/gift-histories", { params });
}

export function createManualHistoryEntry(payload) {
  return http.post("/gift-histories", payload);
}
