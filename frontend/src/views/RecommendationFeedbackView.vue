<script setup>
// SCR-AI-002 · UC11·UC12 피드백 후 재추천
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useGiftStore } from "../store/gift";

const router = useRouter();
const giftStore = useGiftStore();
const reason = ref(null);

const reasonOptions = ["취향과 안 맞음", "이미 있음", "부담스러움"];

async function like() {
  await giftStore.sendFeedback({ liked: true });
  router.push({ name: "SCR-HISTORY-001" }); // UC13 선물 확정 흐름으로 이어짐
}

async function dislikeAndReRecommend() {
  await giftStore.sendFeedback({ liked: false, reason: reason.value });
  const updated = await giftStore.reRecommend();
  router.push({ name: "SCR-AI-001", params: { recommendationId: updated.id } });
}
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>이 추천 어떠세요?</h2>
    <p class="text-muted">피드백 · 재추천</p>
    <div class="d-flex gap-3 mb-3">
      <button class="btn btn-outline-success flex-fill" @click="like">👍 좋아요</button>
      <button class="btn btn-outline-danger flex-fill" @click="dislikeAndReRecommend">👎 싫어요</button>
    </div>
    <label class="form-label small text-uppercase text-muted">싫어요 이유 (선택 시)</label>
    <select class="form-select mb-3" v-model="reason">
      <option v-for="r in reasonOptions" :key="r" :value="r">{{ r }}</option>
    </select>
    <button class="btn btn-dark w-100" @click="dislikeAndReRecommend">다시 추천받기</button>
  </section>
</template>
