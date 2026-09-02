<script setup>
// SCR-CALENDAR-001 · UC4·UC5 (선택 흐름) Google Calendar 연동
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { fetchCalendarEvents, saveSelectedAnniversary } from "../api/calendar";

const router = useRouter();
const events = ref([]);
const selected = ref(null);

onMounted(async () => {
  events.value = await fetchCalendarEvents();
});

async function applySelection() {
  await saveSelectedAnniversary({ eventId: selected.value });
  router.push({ name: "SCR-GIFT-001" });
}
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>Calendar 연동</h2>
    <p class="text-muted">기념일 선택</p>
    <ul class="list-group mb-3">
      <li v-for="e in events" :key="e.id" class="list-group-item">
        <input type="radio" class="form-check-input me-2" :value="e.id" v-model="selected" />
        {{ e.title }} · {{ e.date }}
      </li>
    </ul>
    <button class="btn btn-success w-100" @click="applySelection">선택한 일정 반영</button>
  </section>
</template>
