<script setup>
// SCR-AI-001 ★핵심 · UC9·UC10 AI 추천 결과 확인
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useGiftStore } from "../store/gift";

const props = defineProps({ recommendationId: { type: String, required: true } });
const router = useRouter();
const giftStore = useGiftStore();

onMounted(async () => {
  if (!giftStore.recommendation || giftStore.recommendation.id !== props.recommendationId) {
    await giftStore.refreshRecommendation();
  }
});

function goToFeedback() {
  router.push({ name: "SCR-AI-002", params: { recommendationId: props.recommendationId } });
}
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>추천 결과</h2>
    <p class="text-muted">{{ giftStore.recommendation?.candidates?.length ?? 0 }}개 후보</p>
    <div
      v-for="c in giftStore.recommendation?.candidates"
      :key="c.name"
      class="border rounded p-3 mb-3"
    >
      <div class="fw-bold">{{ c.name }}</div>
      <div class="text-success small fw-bold">약 {{ c.price }}원</div>
      <div class="text-muted small mt-1">{{ c.reason }}</div>
    </div>
    <button class="btn btn-outline-dark w-100" @click="goToFeedback">피드백 남기기</button>
  </section>
</template>
