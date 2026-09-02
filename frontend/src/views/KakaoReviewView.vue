<script setup>
// SCR-KAKAO-002 · UC8 추출 취향 검토·확정
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { fetchExtractedPreferences, confirmExtractedPreferences } from "../api/kakao";

const router = useRouter();
const preferences = ref([]);

onMounted(async () => {
  preferences.value = await fetchExtractedPreferences("latest");
});

async function confirmAndApply() {
  await confirmExtractedPreferences("latest", { preferences: preferences.value });
  router.push({ name: "SCR-GIFT-001" });
}
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>추출 취향 검토</h2>
    <p class="text-muted">확인 후 확정</p>
    <ul class="list-group mb-3">
      <li v-for="(p, idx) in preferences" :key="idx" class="list-group-item">
        <input type="checkbox" class="form-check-input me-2" v-model="p.approved" />
        <strong>{{ p.label }}</strong> — {{ p.value }}
      </li>
    </ul>
    <button class="btn btn-success w-100" @click="confirmAndApply">확정하고 반영</button>
  </section>
</template>
