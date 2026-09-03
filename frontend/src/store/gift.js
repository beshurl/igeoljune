import { defineStore } from "pinia";
import {
  createGiftCondition,
  fetchGiftCondition,
  requestRecommendation,
  fetchRecommendation,
  requestReRecommendation,
  putCandidateFeedback,
} from "../api/recommendations";

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// PROCESSING 상태가 끝날 때까지 폴링
async function pollUntilReady(recommendationId, { interval = 900, maxTries = 20 } = {}) {
  for (let i = 0; i < maxTries; i++) {
    const rec = await fetchRecommendation(recommendationId);
    if (rec.status !== "PROCESSING") return rec;
    await sleep(interval);
  }
  throw new Error("추천 생성이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.");
}

export const useGiftStore = defineStore("gift", {
  state: () => ({
    conditionId: null,
    condition: null, // 예산·목적 등 (반영 정보 표시용)
    recommendation: null,
    loading: false,
    error: null,
  }),
  actions: {
    // SCR-GIFT-001 추천 조건 생성
    async submitCondition(recipientId, payload) {
      this.loading = true;
      try {
        const condition = await createGiftCondition(recipientId, payload);
        this.conditionId = condition.conditionId;
        this.condition = condition;
        return condition;
      } finally {
        this.loading = false;
      }
    },
    // SCR-GIFT-001 -> SCR-AI-001 : AI 추천 요청 (202 PROCESSING 반환)
    async requestRecommendation() {
      this.loading = true;
      this.error = null;
      try {
        const accepted = await requestRecommendation(this.conditionId);
        this.recommendation = accepted; // status: PROCESSING
        return accepted;
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.loading = false;
      }
    },
    // SCR-AI-001 진입 시: 결과 로드 + PROCESSING 이면 완료까지 폴링
    async loadRecommendation(recommendationId) {
      this.loading = true;
      this.error = null;
      try {
        let rec = await fetchRecommendation(recommendationId);
        if (rec.status === "PROCESSING") rec = await pollUntilReady(recommendationId);
        this.recommendation = rec;
        if (rec.conditionId && rec.conditionId !== this.condition?.conditionId) {
          this.condition = await fetchGiftCondition(rec.conditionId).catch(() => this.condition);
        }
        return rec;
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.loading = false;
      }
    },
    // SCR-AI-001 피드백 등록·변경 (후보별 upsert). 응답 feedback 을 해당 후보에 반영
    async sendFeedback(candidateId, { feedbackType, dislikeReason = null }) {
      const fb = await putCandidateFeedback(candidateId, { feedbackType, dislikeReason });
      const c = this.recommendation?.candidates?.find((x) => x.candidateId === candidateId);
      if (c) c.feedback = fb;
      return fb;
    },
    // SCR-AI-002 재추천 (서버가 이전 DISLIKE 를 반영) → 완료까지 폴링
    async reRecommend() {
      this.loading = true;
      try {
        const accepted = await requestReRecommendation(this.recommendation.recommendationId);
        const rec = await pollUntilReady(accepted.recommendationId);
        this.recommendation = rec;
        return rec;
      } finally {
        this.loading = false;
      }
    },
  },
});
