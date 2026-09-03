<script setup>
// SCR-GIFT-001 · 추천 조건 입력 → AI 추천 요청
import { reactive, ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRecipientStore } from "../store/recipient";
import { useGiftStore } from "../store/gift";
import { OCCASION_TYPE, RELATIONSHIP, toOptions, labelOf } from "../constants/enums";
import { extractApiError } from "../utils/apiError";

const props = defineProps({ recipientId: { type: String, required: true } });
const router = useRouter();
const recipientStore = useRecipientStore();
const giftStore = useGiftStore();

const OCCASION_OPTS = toOptions(OCCASION_TYPE);
const OCCASION_ICON = {
  BIRTHDAY: "cake",
  HOUSEWARMING: "home",
  GRADUATION: "school",
  EMPLOYMENT: "work",
  PROMOTION: "trending_up",
  THANKS: "volunteer_activism",
  ETC: "category",
};
const BUDGET_PRESETS = [
  { label: "3~5만원대", min: 30000, max: 50000 },
  { label: "5~10만원대", min: 50000, max: 100000 },
  { label: "10~20만원대", min: 100000, max: 200000 },
  { label: "20만원 이상", min: 200000, max: 300000 },
];

const rid = computed(() => Number(props.recipientId));
const form = reactive({
  budgetMin: null,
  budgetMax: null,
  occasionType: "BIRTHDAY",
  occasionDate: "",
  preferenceNote: "",
  avoidGiftNote: "",
});
const submitting = ref(false);
const submitted = ref(false);
const error = ref("");
const fieldErrors = ref({});

const isBudget = (v) => v != null && v !== "" && !Number.isNaN(Number(v));
const budgetMissing = computed(() => !isBudget(form.budgetMin) || !isBudget(form.budgetMax));
const budgetNegative = computed(
  () => (isBudget(form.budgetMin) && Number(form.budgetMin) < 0) ||
    (isBudget(form.budgetMax) && Number(form.budgetMax) < 0)
);
const budgetInvalid = computed(
  () =>
    isBudget(form.budgetMin) &&
    isBudget(form.budgetMax) &&
    Number(form.budgetMin) > Number(form.budgetMax)
);
const budgetError = computed(() => {
  if (!submitted.value && !fieldErrors.value.budgetMin && !fieldErrors.value.budgetMax) return "";
  if (fieldErrors.value.budgetMax) return fieldErrors.value.budgetMax;
  if (fieldErrors.value.budgetMin) return fieldErrors.value.budgetMin;
  if (budgetMissing.value) return "최소·최대 예산을 모두 입력해 주세요.";
  if (budgetNegative.value) return "예산은 0 이상이어야 합니다.";
  if (budgetInvalid.value) return "최소 예산은 최대 예산보다 클 수 없습니다.";
  return "";
});
const canSubmit = computed(
  () => !budgetMissing.value && !budgetNegative.value && !budgetInvalid.value
);

const recipient = computed(
  () => recipientStore.recipients.find((r) => r.recipientId === rid.value) || null
);

onMounted(async () => {
  recipientStore.select(rid.value);
  try {
    if (!recipientStore.recipients.length) await recipientStore.loadRecipients();
  } catch (e) {
    error.value = extractApiError(e, "대상 정보를 불러오지 못했습니다.").message;
  }
});

function applyPreset(p) {
  form.budgetMin = p.min;
  form.budgetMax = p.max;
}
const presetActive = (p) => form.budgetMin === p.min && form.budgetMax === p.max;

function goKakao() {
  router.push({ name: "SCR-KAKAO-001" });
}

async function submit() {
  if (submitting.value) return;
  submitted.value = true;
  error.value = "";
  fieldErrors.value = {};
  if (!canSubmit.value) return; // budgetError 가 화면에 표시됨
  submitting.value = true;
  try {
    await giftStore.submitCondition(rid.value, { ...form });
    const accepted = await giftStore.requestRecommendation();
    router.push({ name: "SCR-AI-001", params: { recommendationId: accepted.recommendationId } });
  } catch (e) {
    const parsed = extractApiError(e, "추천 요청에 실패했습니다.");
    error.value = parsed.message;
    fieldErrors.value = parsed.fieldErrors;
    submitting.value = false;
  }
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen screen--narrow">
      <p class="page-eyebrow">SCR-GIFT-001 · UC3 · STEP 02</p>
      <h1 class="page-title">맞춤 추천 설계</h1>
      <p class="page-desc">예산과 상황, 취향을 입력하면 AI가 조건을 종합해 선물 후보와 추천 이유를 제공합니다.</p>

      <div class="card card--pad target" v-if="recipient">
        <span class="target__avatar">{{ recipient.name.slice(0, 1) }}</span>
        <div class="target__info">
          <strong>{{ recipient.name }}</strong>
          <span class="pill pill--accent">{{ labelOf(RELATIONSHIP, recipient.relationship) }}</span>
        </div>
        <button class="btn--link" @click="router.push({ name: 'SCR-RECIPIENT-001' })">
          <span class="material-symbols-outlined">swap_horiz</span> 대상 변경
        </button>
      </div>

      <div class="card card--pad block">
        <div class="field">
          <label class="field__label">선물 목적</label>
          <div class="occasions">
            <button
              v-for="o in OCCASION_OPTS"
              :key="o.value"
              class="occ"
              :class="{ 'occ--on': form.occasionType === o.value }"
              @click="form.occasionType = o.value"
            >
              <span class="material-symbols-outlined">{{ OCCASION_ICON[o.value] }}</span>
              {{ o.label }}
            </button>
          </div>
        </div>

        <div class="field">
          <label class="field__label">예산 범위 설정</label>
          <div class="input-row">
            <input class="input" type="number" v-model.number="form.budgetMin" placeholder="최소 50,000" />
            <span class="muted">~</span>
            <input class="input" type="number" v-model.number="form.budgetMax" placeholder="최대 100,000" />
            <span class="muted">원</span>
          </div>
          <div class="chips" style="margin-top: 10px">
            <button
              v-for="p in BUDGET_PRESETS"
              :key="p.label"
              class="chip"
              :class="{ 'chip--on': presetActive(p) }"
              @click="applyPreset(p)"
            >
              {{ p.label }}
            </button>
          </div>
          <p v-if="budgetError" class="form-error">{{ budgetError }}</p>
        </div>

        <div class="field">
          <label class="field__label">선물 전달 예정일 <span class="muted">(선택)</span></label>
          <input class="input" type="date" v-model="form.occasionDate" />
        </div>

        <div class="field">
          <label class="field__label">반영할 취향 메모 <span class="muted">(선택)</span></label>
          <textarea class="textarea" v-model="form.preferenceNote" placeholder="예: 홈카페·인테리어를 좋아하고 실용적인 선물을 선호합니다" />
        </div>

        <div class="field" style="margin-bottom: 0">
          <label class="field__label">피하고 싶은 선물 · 배제 조건 <span class="muted">(선택)</span></label>
          <textarea class="textarea" v-model="form.avoidGiftNote" placeholder="예: 향수는 이미 있고, 부피가 큰 물건은 제외해 주세요" />
        </div>
      </div>

      <button class="card card--pad kakao-cta" @click="goKakao">
        <span class="material-symbols-outlined">forum</span>
        <div>
          <strong>카카오톡 대화에서 취향 가져오기</strong>
          <p class="muted">대화 내보내기 파일을 올리면 AI가 취향 단서를 추출해 취향 목록에 채워줍니다.</p>
        </div>
        <span class="material-symbols-outlined">chevron_right</span>
      </button>

      <InlineAlert type="error" :message="error" />

      <div class="submit">
        <span class="muted">제외 조건에 해당하는 후보는 생성되지 않습니다.</span>
        <button
          class="btn btn--primary btn--lg"
          :disabled="submitting"
          @click="submit"
        >
          <span class="material-symbols-outlined">auto_awesome</span>
          {{ submitting ? "AI 추천 요청 중..." : "조건으로 AI 선물 추천받기" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.target {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  margin-bottom: 18px;
}
.target__avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary);
  display: grid;
  place-items: center;
  font-weight: 800;
}
.target__info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.target__info strong {
  font-size: 16px;
}
.target .btn--link {
  display: flex;
  align-items: center;
  gap: 4px;
}
.target .material-symbols-outlined {
  font-size: 16px;
}
.block {
  margin-bottom: 16px;
}
.occasions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.occ {
  font: inherit;
  font-size: 12.5px;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 6px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text-muted);
  cursor: pointer;
}
.occ:hover {
  border-color: var(--primary-border);
}
.occ--on {
  border: 1.5px solid var(--primary);
  background: var(--primary-soft);
  color: var(--primary);
}
.occ .material-symbols-outlined {
  font-size: 20px;
}
.kakao-cta {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  width: 100%;
  text-align: left;
  border: 1px solid var(--border);
  cursor: pointer;
}
.kakao-cta:hover {
  border-color: var(--primary-border);
  background: var(--surface-tint);
}
.kakao-cta > .material-symbols-outlined:first-child {
  color: var(--primary);
  font-size: 22px;
}
.kakao-cta p {
  font-size: 12.5px;
  margin: 2px 0 0;
}
.kakao-cta > .material-symbols-outlined:last-child {
  margin-left: auto;
  color: var(--text-faint);
}
.submit {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 22px;
}
.submit .muted {
  font-size: 13px;
}
.submit .btn .material-symbols-outlined {
  font-size: 18px;
}
@media (max-width: 620px) {
  .occasions {
    grid-template-columns: repeat(3, 1fr);
  }
  .submit {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
