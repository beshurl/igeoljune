<script setup>
// SCR-HISTORY-001 · UC13·UC14 선물 이력
import { ref, onMounted } from "vue";
import { fetchGiftHistory } from "../api/history";

const histories = ref([]);

onMounted(async () => {
  histories.value = await fetchGiftHistory();
});
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>선물 이력</h2>
    <p class="text-muted">대상별 타임라인</p>
    <ul class="list-group mb-3">
      <li v-for="h in histories" :key="h.id" class="list-group-item">
        <div class="fw-bold">{{ h.recipientName }} · {{ h.occasion }}</div>
        <div class="text-muted small">{{ h.date }} · {{ h.giftName }}</div>
      </li>
    </ul>
    <button class="btn btn-dark w-100">+ 직접 등록</button>
  </section>
</template>
