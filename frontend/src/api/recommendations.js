import http from "./http";

// ===== 대표 흐름: UC7(조건 입력) -> UC8(AI 추천 요청) -> UC9(결과 확인) =====

// SCR-GIFT-001 · UC7 선물 조건 입력 저장
export function createGiftCondition(recipientId, payload) {
  return http.post(`/recipients/${recipientId}/gift-conditions`, payload);
}

// SCR-GIFT-001 · UC8 AI 선물 추천 요청 (AI 확장 지점)
export function requestRecommendation(giftConditionId) {
  return http.post(`/gift-conditions/${giftConditionId}/recommendations`);
}

// SCR-AI-001 · UC9 추천 결과 확인
export function fetchRecommendation(recommendationId) {
  return http.get(`/recommendations/${recommendationId}`);
}

// SCR-AI-002 · UC10·UC11 좋아요/싫어요 피드백 후 재추천
export function submitRecommendationFeedback(recommendationId, payload) {
  return http.post(`/recommendations/${recommendationId}/feedback`, payload);
}

// SCR-AI-002 · UC12 재추천 요청
export function requestReRecommendation(recommendationId) {
  return http.post(`/recommendations/${recommendationId}/re-recommend`);
}

// SCR-HISTORY-001 · UC13 선물 확정 및 이력 저장
export function confirmGift(recommendationId, payload) {
  return http.post(`/recommendations/${recommendationId}/confirm`, payload);
}

// SCR-RANKING-001 실시간 선물 랭킹 조회 (익명 집계)
export function fetchGiftRanking() {
  return http.get("/rankings/gifts");
}
