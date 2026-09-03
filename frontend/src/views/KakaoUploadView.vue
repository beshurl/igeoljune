<script setup>
// SCR-KAKAO-001 · UC6·UC7 (선택 흐름) 카카오톡 대화 파일 업로드
import { ref } from "vue";
import { useRouter } from "vue-router";
import { uploadKakaoChat } from "../api/kakao";

const router = useRouter();
const file = ref(null);

function onFileChange(e) {
  file.value = e.target.files[0];
}

async function startAnalysis() {
  await uploadKakaoChat(file.value);
  router.push({ name: "SCR-KAKAO-002" });
}
</script>

<template>
  <section class="container py-4" style="max-width: 480px;">
    <h2>대화 파일 분석</h2>
    <p class="text-muted">카카오톡 업로드</p>
    <div class="border border-2 border-dashed rounded p-4 text-center text-muted mb-3">
      <input type="file" accept=".txt" @change="onFileChange" />
      <p class="mt-2 mb-0 small">대화 내보내기 파일을 올려주세요 (.txt)</p>
    </div>
    <button class="btn btn-dark w-100" :disabled="!file" @click="startAnalysis">분석 시작</button>
    <p class="text-muted small mt-2">분석 후 원본 파일은 삭제됩니다</p>
  </section>
</template>
