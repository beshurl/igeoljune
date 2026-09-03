<script setup>
// SCR-KAKAO-001 · 카카오톡 대화 파일 임시 분석
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useRecipientStore } from "../store/recipient";
import { useKakaoStore } from "../store/kakao";

const router = useRouter();
const recipientStore = useRecipientStore();
const kakao = useKakaoStore();

const fileInput = ref(null);
const file = ref(null);
const progress = ref(0);
const error = ref("");

const recipientId = computed(() => recipientStore.selectedRecipientId);

function pick() {
  fileInput.value?.click();
}
function onChange(e) {
  file.value = e.target.files[0] || null;
}
function onDrop(e) {
  file.value = e.dataTransfer.files[0] || null;
}

async function start() {
  if (!file.value || kakao.analyzing) return;
  if (!recipientId.value) {
    error.value = "먼저 선물 대상을 선택해 주세요.";
    return;
  }
  error.value = "";
  progress.value = 10;
  const timer = setInterval(() => (progress.value = Math.min(progress.value + 12, 90)), 160);
  try {
    await kakao.analyze(recipientId.value, file.value);
    progress.value = 100;
    router.push({ name: "SCR-KAKAO-002" });
  } catch (e) {
    error.value = e?.response?.data?.message || "분석에 실패했습니다.";
  } finally {
    clearInterval(timer);
  }
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen screen--narrow">
      <p class="page-eyebrow">SCR-KAKAO-001 · UC4 · UC5</p>
      <h1 class="page-title">카카오톡 대화 파일 분석</h1>
      <p class="page-desc">
        대화 내보내기 텍스트 파일(.txt)에서 일상적 관심사, 인테리어 언급, 위시리스트 신호만 선별 추출합니다.
        원문은 저장되지 않습니다.
      </p>

      <div
        class="drop"
        :class="{ 'drop--has': file }"
        @click="pick"
        @dragover.prevent
        @drop.prevent="onDrop"
      >
        <span class="material-symbols-outlined">upload_file</span>
        <p class="drop__title">{{ file ? file.name : "파일을 여기로 끌어놓거나 클릭하여 업로드" }}</p>
        <p class="muted drop__hint">카카오톡 대화 내보내기 (.txt) 파일 1개</p>
        <input ref="fileInput" type="file" accept=".txt" hidden @change="onChange" />
      </div>

      <div class="note">
        <ul>
          <li>관심사, 선호/비선호, 희망 품목 등 선물 관련 정보만 추출합니다.</li>
          <li>대화 전체 요약, 개인정보(주소·연락처·계좌 등)는 추출하지 않습니다.</li>
          <li>분석이 끝나면 원문 파일과 임시 분석 데이터는 즉시 파기됩니다.</li>
        </ul>
      </div>

      <div v-if="kakao.analyzing" class="bar"><div class="bar__fill" :style="{ width: progress + '%' }" /></div>
      <p v-if="error" class="form-error" style="margin-top: 12px">{{ error }}</p>

      <div class="actions">
        <button class="btn btn--outline" @click="pick">파일 선택</button>
        <button class="btn btn--primary" :disabled="!file || kakao.analyzing" @click="start">
          {{ kakao.analyzing ? "분석 중..." : "분석 시작" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.drop {
  border: 1.5px dashed var(--border-strong);
  border-radius: var(--radius-lg);
  padding: 48px 24px;
  text-align: center;
  cursor: pointer;
  background: var(--surface);
  transition: border-color 0.15s, background 0.15s;
}
.drop:hover {
  border-color: var(--primary);
}
.drop--has {
  border-color: var(--primary);
  background: var(--primary-soft);
}
.drop .material-symbols-outlined {
  font-size: 30px;
  color: var(--primary);
}
.drop__title {
  font-weight: 700;
  font-size: 14px;
  margin: 10px 0 4px;
}
.drop__hint {
  font-size: 12.5px;
}
.note {
  margin-top: 16px;
}
.bar {
  height: 6px;
  border-radius: 999px;
  background: var(--border);
  margin-top: 18px;
  overflow: hidden;
}
.bar__fill {
  height: 100%;
  background: var(--primary);
  transition: width 0.16s linear;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}
</style>
