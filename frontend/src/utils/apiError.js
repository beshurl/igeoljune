// axios 오류에서 사용자용 메시지와 필드별 오류를 추출한다.
// 백엔드/Mock 모두 { code, message, fieldErrors: [{ field, reason }] } 형태를 반환한다.
export function extractApiError(
  e,
  fallback = "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요."
) {
  const data = e?.response?.data;
  const fieldErrors = {};
  if (Array.isArray(data?.fieldErrors)) {
    for (const fe of data.fieldErrors) {
      if (fe?.field) {
        fieldErrors[fe.field] = fe.reason || fe.message || "입력값을 확인해 주세요.";
      }
    }
  }
  return {
    code: data?.code || null,
    message: data?.message || e?.message || fallback,
    fieldErrors,
  };
}

export const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
