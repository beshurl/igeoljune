import http from "./http";

// CONDITION-001 추천 조건 생성 — 응답: { conditionId, recipientId, ... }
// payload: { budgetMin, budgetMax, occasionType, occasionDate, preferenceNote, avoidGiftNote }
export function createGiftCondition(recipientId, payload) {
  return http.post(`/recipients/${recipientId}/gift-conditions`, payload);
}

// CONDITION-002 추천 조건 조회
export function fetchGiftCondition(conditionId) {
  return http.get(`/gift-conditions/${conditionId}`);
}

// RECOMMEND-001 AI 추천 요청 — 응답: 202 { recommendationId, status: "PROCESSING", ... }
export function requestRecommendation(conditionId) {
  return http.post(`/gift-conditions/${conditionId}/recommendations`);
}

// RECOMMEND-002 추천 결과 조회 — status: PROCESSING | SUCCESS | FAILED
export function fetchRecommendation(recommendationId) {
  return http.get(`/recommendations/${recommendationId}`);
}

// RECOMMEND-003 조건별 추천 목록 (생성일 역순)
export function fetchRecommendationsByCondition(conditionId) {
  return http.get(`/gift-conditions/${conditionId}/recommendations`);
}

// RECOMMEND-004 피드백 반영 재추천 — 응답: 202 { recommendationId(new), previousRecommendationId, status: "PROCESSING" }
export function requestReRecommendation(recommendationId) {
  return http.post(`/recommendations/${recommendationId}/re-recommend`);
}

// FEEDBACK-001 피드백 등록·변경 (upsert) — { feedbackType: "LIKE"|"DISLIKE", dislikeReason: <code>|null }
export function putCandidateFeedback(candidateId, payload) {
  return http.put(`/recommendation-candidates/${candidateId}/feedback`, payload);
}

// FEEDBACK-002 피드백 조회 (없으면 404)
export function fetchCandidateFeedback(candidateId) {
  return http.get(`/recommendation-candidates/${candidateId}/feedback`);
}

// FEEDBACK-003 피드백 취소 (204)
export function deleteCandidateFeedback(candidateId) {
  return http.delete(`/recommendation-candidates/${candidateId}/feedback`);
}
