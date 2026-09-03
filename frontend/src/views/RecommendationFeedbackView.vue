<script setup>
// SCR-AI-002 · 재추천 진행 (UC10). 좋아요/싫어요는 SCR-AI-001 에서 처리함
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useGiftStore } from "../store/gift";
import { DISLIKE_REASON, labelOf } from "../constants/enums";

const route = useRoute();
const router = useRouter();
const giftStore = useGiftStore();

const busy = ref(false);
const error = ref("");
const recommendationId = computed(() => route.params.recommendationId);

const rec = computed(() => giftStore.recommendation);
const disliked = computed(
  () => (rec.value?.candidates ?? []).filter((c) => c.feedback?.feedbackType === "DISLIKE")
);
const liked = computed(
  () => (rec.value?.candidates ?? []).filter((c) => c.feedback?.feedbackType === "LIKE")
);
const won = (n) => (n ?? 0).toLocaleString("ko-KR") + "원";

onMounted(() => {
  if (!rec.value || rec.value.recommendationId != recommendationId.value) {
    giftStore.loadRecommendation(recommendationId.value);
  }
});

function back() {
  router.push({ name: "SCR-AI-001", params: { recommendationId: recommendationId.value } });
}
async function reRecommend() {
  if (busy.value) return;
  busy.value = true;
  error.value = "";
  try {
    const updated = await giftStore.reRecommend();
    router.push({ name: "SCR-AI-001", params: { recommendationId: updated.recommendationId } });
  } catch (e) {
    error.value = e?.response?.data?.message || "재추천 요청에 실패했습니다.";
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen screen--narrow">
      <p class="page-eyebrow">SCR-AI-002 · UC10 · REQ-F-010</p>
      <h1 class="page-title">피드백 반영 재추천</h1>
      <p class="page-desc">
        같은 조건으로 새 추천을 실행합니다. 이전 추천에서 <strong>싫어요</strong>로 표시한 후보와 사유가
        재추천 컨텍스트에 반영됩니다.
      </p>

      <div v-if="!rec" class="loading-block"><div class="spinner" /> 이전 추천 정보를 불러오는 중...</div>

      <template v-else>
        <div class="card card--pad panel">
          <div class="panel__h">
            <span class="material-symbols-outlined">thumb_down</span>
            재추천에 반영될 싫어요 후보 ({{ disliked.length }}건)
          </div>
          <ul v-if="disliked.length" class="dl">
            <li v-for="c in disliked" :key="c.candidateId">
              <div>
                <strong>{{ c.giftName }}</strong>
                <span class="muted"> · {{ won(c.estimatedPriceMin) }}~{{ won(c.estimatedPriceMax) }}</span>
              </div>
              <span class="pill pill--danger">{{ labelOf(DISLIKE_REASON, c.feedback.dislikeReason) }}</span>
            </li>
          </ul>
          <p v-else class="muted empty">
            아직 싫어요를 남긴 후보가 없습니다. 추천 결과 화면에서 후보에 싫어요와 사유를 먼저 남겨 주세요.
          </p>

          <div v-if="liked.length" class="keep">
            <span class="material-symbols-outlined">favorite</span>
            좋아요 {{ liked.length }}건은 참고 신호로만 유지되며 재추천에서 제외되지 않습니다.
          </div>
        </div>

        <p v-if="error" class="form-error">{{ error }}</p>

        <div class="actions">
          <button class="btn--link" @click="back">추천 결과로 돌아가기</button>
          <button class="btn btn--dark btn--lg" :disabled="busy || !disliked.length" @click="reRecommend">
            <span class="material-symbols-outlined">refresh</span>
            {{ busy ? "재추천 중..." : "피드백 반영해 재추천 받기" }}
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.panel__h {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-label);
  font-weight: 700;
  font-size: 13px;
  margin-bottom: 14px;
}
.panel__h .material-symbols-outlined {
  font-size: 17px;
  color: var(--danger);
}
.dl {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.dl li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.dl strong {
  font-size: 13.5px;
}
.empty {
  font-size: 13px;
  padding: 8px 0;
}
.keep {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--text-muted);
}
.keep .material-symbols-outlined {
  font-size: 15px;
  color: var(--primary);
}
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 20px;
}
.actions .btn .material-symbols-outlined {
  font-size: 18px;
}
</style>
