import http from "./http";

// PREF-003 취향 목록 조회 — 응답: { items, totalCount }
// filters: { preferenceType?, sourceType? }
export function fetchPreferences(recipientId, params = {}) {
  return http.get(`/recipients/${recipientId}/preferences`, { params });
}

// PREF-001 직접 취향 등록 (sourceType 은 서버가 DIRECT 로 설정)
export function createPreference(recipientId, { preferenceType, preferenceValue }) {
  return http.post(`/recipients/${recipientId}/preferences`, {
    preferenceType,
    preferenceValue,
  });
}

// PREF-002 추출 취향 일괄 저장 — { sourceType: "KAKAO", items: [{ preferenceType, preferenceValue }] }
export function savePreferencesBulk(recipientId, { sourceType, items }) {
  return http.post(`/recipients/${recipientId}/preferences/bulk`, { sourceType, items });
}

// PREF-004 취향 수정
export function updatePreference(preferenceId, payload) {
  return http.patch(`/preferences/${preferenceId}`, payload);
}

// PREF-005 취향 삭제 (204)
export function deletePreference(preferenceId) {
  return http.delete(`/preferences/${preferenceId}`);
}

// KAKAO-001 카카오톡 파일 임시 분석 (multipart) — 응답: { items: [{ preferenceType, preferenceValue }] }
export function analyzeKakaoFile(recipientId, file) {
  const form = new FormData();
  form.append("file", file);
  return http.post(`/recipients/${recipientId}/kakao-analysis`, form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}
