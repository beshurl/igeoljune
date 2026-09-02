<script setup>
// SCR-GIFT-001 · UC3(대표 흐름 UC7) 추천 조건 입력
import { reactive } from "vue";
import { useRouter } from "vue-router";
import { useGiftStore } from "../store/gift";

const props = defineProps({ recipientId: { type: String, required: true } });
const router = useRouter();
const giftStore = useGiftStore();

const form = reactive({
  budget: null,
  anniversaryDate: null,
  preferenceTags: [],
  excludeTags: [],
});

async function requestAiRecommendation() {
  const condition = await giftStore.submitCondition(props.recipientId, form);
  const recommendation = await giftStore.requestRecommendation();
  router.push({ name: "SCR-AI-001", params: { recommendationId: recommendation.id } });
}
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>추천 조건 입력</h2>
    <div class="mb-3">
      <label class="form-label small text-uppercase text-muted">예산</label>
      <input type="number" class="form-control" v-model.number="form.budget" placeholder="예: 50000" />
    </div>
    <div class="mb-3">
      <label class="form-label small text-uppercase text-muted">기념일</label>
      <input type="date" class="form-control" v-model="form.anniversaryDate" />
    </div>
    <div class="mb-3">
      <label class="form-label small text-uppercase text-muted">취향 · 제외 조건</label>
      <input
        type="text"
        class="form-control"
        placeholder="쉼표로 구분 (예: 홈카페, 강한 향 제외)"
        @change="(e) => (form.preferenceTags = e.target.value.split(','))"
      />
    </div>
    <button class="btn btn-success w-100" @click="requestAiRecommendation">AI 추천 요청하기</button>
  </section>
</template>
