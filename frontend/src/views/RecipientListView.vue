<script setup>
// SCR-RECIPIENT-001 · UC2 추천 대상 관리
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRecipientStore } from "../store/recipient";

const router = useRouter();
const recipientStore = useRecipientStore();

onMounted(() => {
  recipientStore.loadRecipients();
});

function goToGiftCondition(recipientId) {
  recipientStore.select(recipientId);
  router.push({ name: "SCR-GIFT-001", params: { recipientId } });
}
</script>

<template>
  <section class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2>선물 대상</h2>
      <button class="btn btn-outline-dark btn-sm">+ 새 대상</button>
    </div>
    <ul class="list-group">
      <li
        v-for="r in recipientStore.recipients"
        :key="r.id"
        class="list-group-item d-flex justify-content-between align-items-center"
        role="button"
        @click="goToGiftCondition(r.id)"
      >
        <span>{{ r.name }} · {{ r.relationship }}</span>
        <span class="text-muted small">{{ r.upcomingAnniversary }}</span>
      </li>
    </ul>
  </section>
</template>
