<script setup>
// 화면 공통 인라인 알림 (에러/경고/성공). message 가 있을 때만 렌더.
import { computed } from "vue";

const props = defineProps({
  type: { type: String, default: "error" }, // error | warning | success | info
  message: { type: String, default: "" },
  retry: { type: Boolean, default: false },
});
defineEmits(["retry"]);

const icon = computed(
  () =>
    ({ error: "error", warning: "warning", success: "check_circle", info: "info" }[props.type] ||
    "error")
);
</script>

<template>
  <div v-if="message" class="alert" :class="`alert--${type}`" role="alert">
    <span class="material-symbols-outlined">{{ icon }}</span>
    <span class="alert__msg">{{ message }}</span>
    <button v-if="retry" class="alert__retry" type="button" @click="$emit('retry')">
      다시 시도
    </button>
  </div>
</template>

<style scoped>
.alert {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: var(--radius);
  padding: 11px 14px;
  font-size: 13px;
  margin-bottom: 16px;
  border: 1px solid;
}
.alert .material-symbols-outlined {
  font-size: 17px;
  flex-shrink: 0;
}
.alert__msg {
  flex: 1;
}
.alert__retry {
  flex-shrink: 0;
  font: inherit;
  font-size: 12px;
  font-weight: 700;
  background: transparent;
  border: 1px solid currentColor;
  border-radius: 999px;
  padding: 4px 10px;
  cursor: pointer;
  color: inherit;
}
.alert--error {
  background: var(--danger-soft);
  color: #c22b2d;
  border-color: #f6c9ca;
}
.alert--warning {
  background: var(--warning-soft);
  color: var(--warning);
  border-color: #f2d9b6;
}
.alert--success {
  background: var(--success-soft);
  color: var(--success);
  border-color: #bfe2cd;
}
.alert--info {
  background: var(--primary-soft);
  color: #a94f00;
  border-color: var(--primary-border);
}
</style>
