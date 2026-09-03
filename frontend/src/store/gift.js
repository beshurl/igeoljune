import { defineStore } from "pinia";
import {
  createGiftCondition,
  fetchGiftCondition,
  requestRecommendation,
  fetchRecommendation,
  requestReRecommendation,
  putCandidateFeedback,
  selectCandidate as apiSelectCandidate,
  deselectCandidate as apiDeselectCandidate,
} from "../api/recommendations";

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
    // SCR-GIFT-001 -> SCR-AI-001 : AI 추천 요청.
    // 우리 서비스는 AI 를 Mock 으로 대체하므로 처리 중 단계 없이 201 응답에 후보 전체가 온다.
    async requestRecommendation() {
      this.loading = true;
      this.error = null;
      try {
        const rec = await requestRecommendation(this.conditionId);
        this.recommendation = rec;
        return rec;
      } catch (e) {
        this.error = e;
        throw e;
      } finally {
        this.loading = false;
      }
    },
    // SCR-AI-001 진입/새로고침 시 결과 로드
    async loadRecommendation(recommendationId) {
      this.loading = true;
      this.error = null;
      try {
        const rec = await fetchRecommendation(recommendationId);
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
    // SCR-AI-002 재추천 (서버가 이전 DISLIKE 를 반영) — 201 응답에 새 후보 전체가 온다
    async reRecommend() {
      this.loading = true;
      try {
        const rec = await requestReRecommendation(this.recommendation.recommendationId);
        this.recommendation = rec;
        return rec;
      } finally {
        this.loading = false;
      }
    },
    // SCR-AI-001 최종 선물 선택 (추천 실행당 1건 — 다른 후보 선택은 자동 해제)
    async selectCandidate(candidateId) {
      const updated = await apiSelectCandidate(candidateId);
      for (const c of this.recommendation?.candidates ?? []) {
        if (c.candidateId === candidateId) Object.assign(c, updated);
        else c.selectedAt = null;
      }
      return updated;
    },
    async deselectCandidate(candidateId) {
      await apiDeselectCandidate(candidateId);
      const c = this.recommendation?.candidates?.find((x) => x.candidateId === candidateId);
      if (c) c.selectedAt = null;
    },
  },
});
