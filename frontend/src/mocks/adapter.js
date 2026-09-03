// 백엔드 없이 화면·흐름을 확인하기 위한 목(mock) axios 어댑터.
// API 명세서 v1 기준. http.js 에서 VITE_USE_MOCK !== "false" 일 때 주입됩니다.
// 실제 연동 시 .env 에 VITE_USE_MOCK=false.

const delay = (ms = 240) => new Promise((r) => setTimeout(r, ms));

const state = {
  user: { userId: 1, email: "demo@igeoljune.dev", name: "데모 사용자" },
  recipients: [
    { recipientId: 1, name: "김지은", relationship: "FRIEND", ageGroup: "LATE_20S", gender: "FEMALE", job: "회사원", createdAt: "2026-09-01T09:00:00+09:00", updatedAt: "2026-09-01T09:00:00+09:00" },
    { recipientId: 2, name: "박서준", relationship: "LOVER", ageGroup: "LATE_20S", gender: "MALE", job: "직장인", createdAt: "2026-09-01T09:00:00+09:00", updatedAt: "2026-09-01T09:00:00+09:00" },
    { recipientId: 3, name: "최민수", relationship: "COWORKER", ageGroup: "THIRTIES", gender: "MALE", job: "", createdAt: "2026-09-01T09:00:00+09:00", updatedAt: "2026-09-01T09:00:00+09:00" },
  ],
  preferences: {}, // recipientId -> [pref]
  previousGifts: {}, // recipientId -> [gift]
  conditions: {}, // conditionId -> condition
  recommendations: {}, // recommendationId -> recommendation
  feedbacks: {}, // candidateId -> feedback
  seq: 100,
};
const nextId = () => ++state.seq;
const now = () => new Date().toISOString();

const KAKAO_ITEMS = [
  { preferenceType: "INTEREST", preferenceValue: "홈카페, 러닝" },
  { preferenceType: "PREFERRED_ATTRIBUTE", preferenceValue: "무향, 미니멀 디자인" },
  { preferenceType: "DISLIKED_CATEGORY", preferenceValue: "강한 향이 나는 제품" },
  { preferenceType: "WISH_ITEM", preferenceValue: "무선 충전기" },
];

function makeCandidates() {
  return [
    {
      candidateId: nextId(),
      giftName: "홈카페 드립 세트",
      giftCategory: "HOME_CAFE",
      estimatedPriceMin: 45000,
      estimatedPriceMax: 55000,
      recommendationReason:
        "친구의 생일 상황과 예산, 홈카페·인테리어 취향을 함께 고려했습니다. 과거에 텀블러를 선물한 이력이 있어 중복을 피했고 강한 향과는 무관한 후보입니다.",
      consideredInfo: "홈카페·인테리어 취향 · 생일 · 4~6만 원 예산 · 텀블러 보유",
      cautionNote: "이미 보유한 드립 도구가 있는지 확인해 주세요.",
      recommendRank: 1,
      feedback: null,
    },
    {
      candidateId: nextId(),
      giftName: "스페셜티 원두 구독권",
      giftCategory: "HOME_CAFE",
      estimatedPriceMin: 28000,
      estimatedPriceMax: 35000,
      recommendationReason:
        "홈카페 취향을 반영하면서 예산을 전부 사용하지 않는 대안입니다. 소모품이라 부담 없이 받을 수 있습니다.",
      consideredInfo: "홈카페 취향 · 생일 · 소모품 선호",
      cautionNote: "원두 로스팅 강도 선호를 확인하면 좋습니다.",
      recommendRank: 2,
      feedback: null,
    },
    {
      candidateId: nextId(),
      giftName: "미니멀 커피잔 세트",
      giftCategory: "LIVING",
      estimatedPriceMin: 42000,
      estimatedPriceMax: 50000,
      recommendationReason:
        "인테리어 취향과 무향 선호를 함께 반영한 후보입니다. 텀블러와 달리 새로운 사용 맥락을 제공합니다.",
      consideredInfo: "인테리어·무향 선호 · 생일",
      cautionNote: "색상 선호가 확인되지 않아 무채색 기준으로 제안했습니다.",
      recommendRank: 3,
      feedback: null,
    },
  ];
}

function makeRecommendation(conditionId, previousRecommendationId = null) {
  const recommendationId = nextId();
  const rec = {
    recommendationId,
    conditionId,
    previousRecommendationId,
    status: "PROCESSING",
    failure: null,
    createdAt: now(),
    updatedAt: now(),
    candidates: [],
    _readyAt: Date.now() + 1400, // 이 시각 이후 GET 하면 SUCCESS
  };
  state.recommendations[recommendationId] = rec;
  return rec;
}

function viewRecommendation(rec) {
  if (rec.status === "PROCESSING" && Date.now() >= rec._readyAt) {
    rec.status = "SUCCESS";
    rec.updatedAt = now();
    rec.candidates = makeCandidates();
  }
  const { _readyAt, ...view } = rec;
  return {
    ...view,
    candidates: view.candidates.map((c) => ({
      ...c,
      feedback: state.feedbacks[c.candidateId] ?? null,
    })),
  };
}

function findCandidate(candidateId) {
  for (const rec of Object.values(state.recommendations)) {
    const c = rec.candidates.find((x) => String(x.candidateId) === String(candidateId));
    if (c) return c;
  }
  return null;
}

const routes = [
  // ---- 인증 ----
  ["POST", /^\/auth\/signup$/, (_m, b) => ({
    userId: state.user.userId,
    email: b.email,
    name: b.name || "새 사용자",
    createdAt: now(),
  })],
  ["POST", /^\/auth\/login$/, (_m, b) => {
    state.user = { ...state.user, email: b.email || state.user.email };
    return {
      accessToken: "mock-token-" + Date.now(),
      tokenType: "Bearer",
      expiresIn: 3600,
      user: state.user,
    };
  }],
  ["GET", /^\/users\/me$/, () => state.user],
  ["PATCH", /^\/users\/me$/, (_m, b) => {
    state.user = { ...state.user, name: b.name ?? state.user.name };
    return state.user;
  }],

  // ---- 추천 대상 ----
  ["GET", /^\/recipients$/, () => ({ items: state.recipients, totalCount: state.recipients.length })],
  ["POST", /^\/recipients$/, (_m, b) => {
    const r = { recipientId: nextId(), ...b, createdAt: now(), updatedAt: now() };
    state.recipients.push(r);
    return r;
  }],
  ["GET", /^\/recipients\/(\d+)$/, (m) =>
    state.recipients.find((r) => r.recipientId === +m[1]) || { __status: 404 }],
  ["PATCH", /^\/recipients\/(\d+)$/, (m, b) => {
    const r = state.recipients.find((x) => x.recipientId === +m[1]);
    if (r) Object.assign(r, b, { updatedAt: now() });
    return r || { __status: 404 };
  }],
  ["DELETE", /^\/recipients\/(\d+)$/, (m) => {
    state.recipients = state.recipients.filter((r) => r.recipientId !== +m[1]);
    return { __status: 204 };
  }],

  // ---- 구조화 취향 ----
  ["GET", /^\/recipients\/(\d+)\/preferences$/, (m) => {
    const list = state.preferences[m[1]] ?? [];
    return { items: list, totalCount: list.length };
  }],
  ["POST", /^\/recipients\/(\d+)\/preferences$/, (m, b) => {
    const pref = { preferenceId: nextId(), ...b, sourceType: "DIRECT" };
    (state.preferences[m[1]] ??= []).push(pref);
    return pref;
  }],
  ["POST", /^\/recipients\/(\d+)\/preferences\/bulk$/, (m, b) => {
    const saved = (b.items ?? []).map((it) => ({
      preferenceId: nextId(),
      ...it,
      sourceType: b.sourceType ?? "KAKAO",
    }));
    (state.preferences[m[1]] ??= []).push(...saved);
    return { items: saved, totalCount: saved.length };
  }],
  ["PATCH", /^\/preferences\/(\d+)$/, (_m, b) => ({ preferenceId: +_m[1], ...b })],
  ["DELETE", /^\/preferences\/(\d+)$/, () => ({ __status: 204 })],
  ["POST", /^\/recipients\/(\d+)\/kakao-analysis$/, () => ({ items: KAKAO_ITEMS })],

  // ---- 과거 선물 ----
  ["GET", /^\/recipients\/(\d+)\/previous-gifts$/, (m) => {
    const list = state.previousGifts[m[1]] ?? [];
    return { items: list, totalCount: list.length };
  }],
  ["POST", /^\/recipients\/(\d+)\/previous-gifts$/, (m, b) => {
    const g = { previousGiftId: nextId(), recipientId: +m[1], ...b, createdAt: now(), updatedAt: now() };
    (state.previousGifts[m[1]] ??= []).push(g);
    return g;
  }],
  ["PATCH", /^\/previous-gifts\/(\d+)$/, (_m, b) => ({ previousGiftId: +_m[1], ...b })],
  ["DELETE", /^\/previous-gifts\/(\d+)$/, (m) => {
    const id = +m[1];
    for (const k of Object.keys(state.previousGifts)) {
      state.previousGifts[k] = (state.previousGifts[k] || []).filter(
        (g) => g.previousGiftId !== id
      );
    }
    return { __status: 204 };
  }],

  // ---- 추천 조건 ----
  ["POST", /^\/recipients\/(\d+)\/gift-conditions$/, (m, b) => {
    const c = { conditionId: nextId(), recipientId: +m[1], ...b, createdAt: now() };
    state.conditions[c.conditionId] = c;
    return c;
  }],
  ["GET", /^\/gift-conditions\/(\d+)$/, (m) => state.conditions[m[1]] || { __status: 404 }],
  ["PATCH", /^\/gift-conditions\/(\d+)$/, (m, b) => {
    const c = state.conditions[m[1]];
    if (c) Object.assign(c, b);
    return c || { __status: 404 };
  }],
  ["DELETE", /^\/gift-conditions\/(\d+)$/, () => ({ __status: 204 })],

  // ---- AI 추천 ----
  ["POST", /^\/gift-conditions\/(\d+)\/recommendations$/, (m) => {
    const rec = makeRecommendation(+m[1]);
    return { __status: 202, ...viewRecommendation(rec) };
  }],
  ["GET", /^\/gift-conditions\/(\d+)\/recommendations$/, (m) => {
    const list = Object.values(state.recommendations)
      .filter((r) => r.conditionId === +m[1])
      .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
      .map(viewRecommendation);
    return { items: list, totalCount: list.length };
  }],
  ["GET", /^\/recommendations\/(\d+)$/, (m) => {
    const rec = state.recommendations[m[1]];
    return rec ? viewRecommendation(rec) : { __status: 404 };
  }],
  ["POST", /^\/recommendations\/(\d+)\/re-recommend$/, (m) => {
    const prev = state.recommendations[m[1]];
    const rec = makeRecommendation(prev?.conditionId ?? nextId(), +m[1]);
    return { __status: 202, ...viewRecommendation(rec) };
  }],

  // ---- 피드백 ----
  ["PUT", /^\/recommendation-candidates\/(\d+)\/feedback$/, (m, b) => {
    const fb = {
      feedbackId: state.feedbacks[m[1]]?.feedbackId ?? nextId(),
      candidateId: +m[1],
      feedbackType: b.feedbackType,
      dislikeReason: b.feedbackType === "DISLIKE" ? b.dislikeReason ?? null : null,
      createdAt: state.feedbacks[m[1]]?.createdAt ?? now(),
      updatedAt: now(),
    };
    state.feedbacks[m[1]] = fb;
    const c = findCandidate(m[1]);
    if (c) c.feedback = fb;
    return fb;
  }],
  ["GET", /^\/recommendation-candidates\/(\d+)\/feedback$/, (m) =>
    state.feedbacks[m[1]] ?? { __status: 404 }],
  ["DELETE", /^\/recommendation-candidates\/(\d+)\/feedback$/, (m) => {
    delete state.feedbacks[m[1]];
    const c = findCandidate(m[1]);
    if (c) c.feedback = null;
    return { __status: 204 };
  }],
];

const STATUS_TEXT = {
  200: "OK",
  201: "Created",
  202: "Accepted",
  204: "No Content",
  400: "Bad Request",
  401: "Unauthorized",
  403: "Forbidden",
  404: "Not Found",
  409: "Conflict",
  422: "Unprocessable Entity",
  500: "Internal Server Error",
};

const ERROR_CODE = {
  400: "VALIDATION_ERROR",
  401: "UNAUTHORIZED",
  403: "RESOURCE_FORBIDDEN",
  404: "RESOURCE_NOT_FOUND",
  409: "RESOURCE_CONFLICT",
  422: "AI_RESULT_INVALID",
};
const ERROR_MESSAGE = {
  401: "인증이 필요합니다.",
  403: "접근할 수 없는 리소스입니다.",
  404: "요청한 리소스를 찾을 수 없습니다.",
  409: "현재 상태에서는 요청을 처리할 수 없습니다.",
  422: "AI 결과를 처리하지 못했습니다.",
};

// axios 기본 어댑터의 settle 동작 재현: validateStatus 통과 못 하면 AxiosError 로 reject.
// 오류 상태인데 본문이 없으면 API 명세의 ErrorResponse 형태로 채운다.
function settle(status, data, config) {
  const ok = config.validateStatus
    ? config.validateStatus(status)
    : status >= 200 && status < 300;

  let body = status === 204 ? "" : data;
  if (!ok && (!data || !data.code)) {
    body = {
      code: ERROR_CODE[status] || "ERROR",
      message: ERROR_MESSAGE[status] || `요청을 처리할 수 없습니다. (${status})`,
      fieldErrors: [],
    };
  }

  const response = {
    data: body,
    status,
    statusText: STATUS_TEXT[status] || "",
    headers: {},
    config,
    request: {},
  };
  if (ok) return response;

  const error = new Error(`Request failed with status code ${status}`);
  error.config = config;
  error.request = {};
  error.response = response;
  error.isAxiosError = true;
  error.status = status;
  return Promise.reject(error);
}

export default async function mockAdapter(config) {
  await delay();
  const method = (config.method || "get").toUpperCase();
  const url = (config.url || "").split("?")[0];

  let body = config.data;
  if (typeof body === "string") {
    try {
      body = JSON.parse(body);
    } catch {
      body = {};
    }
  }
  if (typeof FormData !== "undefined" && body instanceof FormData) body = { file: "(mock file)" };

  for (const [m, re, handler] of routes) {
    if (m !== method) continue;
    const match = re.exec(url);
    if (!match) continue;
    const result = handler(match, body || {}) ?? {};
    const { __status, ...data } = result;
    return settle(__status || 200, data, config);
  }

  return settle(
    404,
    { code: "MOCK_NOT_FOUND", message: `mock route not found: ${method} ${url}`, fieldErrors: [] },
    config
  );
}
