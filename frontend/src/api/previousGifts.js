import http from "./http";

// PREVGIFT-002 과거 선물 목록 조회 — 응답: { items, totalCount }
export function fetchPreviousGifts(recipientId) {
  return http.get(`/recipients/${recipientId}/previous-gifts`);
}

// PREVGIFT-001 과거 선물 등록 — { giftName, giftCategory, giftedAt, note }
// SCR-AI-001 "선택하기" 는 추천 후보를 이 API 로 과거 선물에 기록하는 방식으로 대체
export function createPreviousGift(recipientId, payload) {
  return http.post(`/recipients/${recipientId}/previous-gifts`, payload);
}

// PREVGIFT-003 수정
export function updatePreviousGift(previousGiftId, payload) {
  return http.patch(`/previous-gifts/${previousGiftId}`, payload);
}

// PREVGIFT-004 삭제 (204)
export function deletePreviousGift(previousGiftId) {
  return http.delete(`/previous-gifts/${previousGiftId}`);
}
