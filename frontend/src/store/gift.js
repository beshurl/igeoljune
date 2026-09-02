import { defineStore } from "pinia";
import {
  createGiftCondition,
  requestRecommendation,
  fetchRecommendation,
  submitRecommendationFeedback,
  requestReRecommendation,
  confirmGift,
} from "../api/recommendations";

// 대표 흐름(UC7->UC8->UC9, 재추천 UC12, 확정 UC13)을 담당하는 스토어
export const useGiftStore = defineStore("gift", {
  state: () => ({
    giftConditionId: null,
    recommendation: null,
    loading: false,
    error: null,
  }),
  actions: {
    // SCR-GIFT-001 · UC7
    async submitCondition(recipientId, payload) {
      this.loading = true;
      try {
        const condition = await createGiftCondition(recipientId, payload);
        this.giftConditionId = condition.id;
        return condition;
      } finally {
        this.loading = false;
      }
    },
    // SCR-GIFT-001 -> SCR-AI-001 · UC8, UC9
    async requestRecommendation() {
      this.loading = true;
      this.error = null;
      try {
        const result = await requestRecommendation(this.giftConditionId);
        this.recommendation = result;
        return result;
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.loading = false;
      }
    },
    async refreshRecommendation() {
      this.recommendation = await fetchRecommendation(this.recommendation.id);
    },
    // SCR-AI-002 · UC10·UC11
    async sendFeedback(payload) {
      return submitRecommendationFeedback(this.recommendation.id, payload);
    },
    // SCR-AI-002 · UC12
    async reRecommend() {
      this.loading = true;
      try {
        this.recommendation = await requestReRecommendation(this.recommendation.id);
        return this.recommendation;
      } finally {
        this.loading = false;
      }
    },
    // UC13
    async confirm(payload) {
      return confirmGift(this.recommendation.id, payload);
    },
  },
});
