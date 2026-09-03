import { defineStore } from "pinia";
import { analyzeKakaoFile, savePreferencesBulk } from "../api/preferences";

// SCR-KAKAO-001 업로드 → SCR-KAKAO-002 검토 사이에서 추출 결과를 들고 있는 스토어
export const useKakaoStore = defineStore("kakao", {
  state: () => ({
    recipientId: null,
    items: [], // [{ preferenceType, preferenceValue, approved }]
    analyzing: false,
  }),
  actions: {
    async analyze(recipientId, file) {
      this.analyzing = true;
      try {
        const res = await analyzeKakaoFile(recipientId, file);
        this.recipientId = recipientId;
        this.items = (res.items ?? []).map((it) => ({ ...it, approved: true }));
        return this.items;
      } finally {
        this.analyzing = false;
      }
    },
    // SCR-KAKAO-002 승인 항목만 KAKAO 소스로 일괄 저장
    async saveApproved() {
      const items = this.items
        .filter((it) => it.approved)
        .map(({ preferenceType, preferenceValue }) => ({ preferenceType, preferenceValue }));
      if (!items.length) return { items: [], totalCount: 0 };
      return savePreferencesBulk(this.recipientId, { sourceType: "KAKAO", items });
    },
    reset() {
      this.recipientId = null;
      this.items = [];
    },
  },
});
