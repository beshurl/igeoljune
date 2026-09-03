// API 명세서 v1 · Enum 정의 및 라벨 매핑
// ※ 명세서에 예시로 명시된 값: FRIEND / LATE_20S / MALE / BIRTHDAY.
//    나머지 코드는 팀 합의 전까지 프론트 잠정값 (백엔드 확정 시 이 파일만 수정).

export const RELATIONSHIP = {
  FRIEND: "친구",
  LOVER: "연인",
  FAMILY: "가족",
  COWORKER: "직장 동료",
  BOSS: "상사",
};

export const AGE_GROUP = {
  TEENS: "10대",
  EARLY_20S: "20대 초반",
  LATE_20S: "20대 후반",
  THIRTIES: "30대",
  FORTIES: "40대",
  FIFTIES_PLUS: "50대 이상",
};

export const GENDER = {
  FEMALE: "여성",
  MALE: "남성",
  OTHER: "기타",
};

export const OCCASION_TYPE = {
  BIRTHDAY: "생일",
  HOUSEWARMING: "집들이",
  GRADUATION: "졸업",
  EMPLOYMENT: "취업",
  PROMOTION: "승진",
  THANKS: "감사",
  ETC: "기타",
};

export const PREFERENCE_TYPE = {
  INTEREST: "관심사",
  PREFERRED_CATEGORY: "선호 카테고리",
  PREFERRED_ATTRIBUTE: "선호 속성",
  DISLIKED_CATEGORY: "비선호",
  WISH_ITEM: "희망 품목",
};

export const SOURCE_TYPE = {
  DIRECT: "직접 입력",
  KAKAO: "카카오톡 분석",
};

export const RECOMMENDATION_STATUS = {
  PROCESSING: "AI 추천 처리 중",
  SUCCESS: "추천 완료",
  FAILED: "추천 실패",
};

export const FEEDBACK_TYPE = {
  LIKE: "좋아요",
  DISLIKE: "싫어요",
};

export const DISLIKE_REASON = {
  TASTE_MISMATCH: "취향과 맞지 않음",
  ALREADY_OWNED: "이미 가지고 있음",
  SIMILAR_TO_PREVIOUS: "과거 선물과 유사함",
  TOO_BURDENSOME: "관계에 비해 부담스러움",
  NOT_PRACTICAL: "실용성이 부족함",
  PRICE_INAPPROPRIATE: "가격이 적절하지 않음",
  WANT_DIFFERENT_STYLE: "다른 스타일을 원함",
  OTHER: "기타",
};

// { CODE: "라벨" } → [{ value: "CODE", label: "라벨" }]
export const toOptions = (map) =>
  Object.entries(map).map(([value, label]) => ({ value, label }));

// 코드 → 라벨 (없으면 코드 그대로)
export const labelOf = (map, code) => map[code] ?? code ?? "-";
