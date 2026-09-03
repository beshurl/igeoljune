<script setup>
// SCR-HISTORY-001 · 과거 선물 관리 (UC11 / REQ-F-011) — previous_gifts CRUD
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRecipientStore } from "../store/recipient";
import { fetchPreviousGifts, createPreviousGift, deletePreviousGift } from "../api/previousGifts";
import { RELATIONSHIP, labelOf } from "../constants/enums";
import { extractApiError } from "../utils/apiError";

const props = defineProps({ recipientId: { type: String, required: true } });
const router = useRouter();
const recipientStore = useRecipientStore();

const rid = computed(() => Number(props.recipientId));
const gifts = ref([]);
const loading = ref(true);
const saving = ref(false);
const loadError = ref("");
const error = ref("");
const form = reactive({ giftName: "", giftCategory: "", giftedAt: "", note: "" });

const recipient = computed(
  () => recipientStore.recipients.find((r) => r.recipientId === rid.value) || null
);

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    recipientStore.select(rid.value);
    if (!recipientStore.recipients.length) await recipientStore.loadRecipients();
    const res = await fetchPreviousGifts(rid.value);
    gifts.value = res.items ?? [];
  } catch (e) {
    loadError.value = extractApiError(e).message;
  } finally {
    loading.value = false;
  }
}
onMounted(load);

async function add() {
  error.value = "";
  if (!form.giftName.trim()) {
    error.value = "선물 이름을 입력해 주세요.";
    return;
  }
  if (saving.value) return;
  saving.value = true;
  try {
    const payload = {
      giftName: form.giftName.trim(),
      giftCategory: form.giftCategory.trim() || null,
      giftedAt: form.giftedAt || null,
      note: form.note.trim() || null,
    };
    const created = await createPreviousGift(rid.value, payload);
    gifts.value.unshift(created);
    Object.assign(form, { giftName: "", giftCategory: "", giftedAt: "", note: "" });
  } catch (e) {
    error.value = extractApiError(e, "과거 선물 저장에 실패했습니다.").message;
  } finally {
    saving.value = false;
  }
}
async function remove(g) {
  if (!confirm(`'${g.giftName}' 기록을 삭제할까요?`)) return;
  error.value = "";
  try {
    await deletePreviousGift(g.previousGiftId);
    gifts.value = gifts.value.filter((x) => x.previousGiftId !== g.previousGiftId);
  } catch (e) {
    error.value = extractApiError(e, "삭제에 실패했습니다.").message;
  }
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen screen--narrow">
      <p class="page-eyebrow">SCR-HISTORY-001 · UC11 · REQ-F-011</p>
      <h1 class="page-title">과거 선물 관리</h1>
      <p class="page-desc">
        <template v-if="recipient">
          <strong>{{ recipient.name }}</strong>
          <span class="pill pill--accent">{{ labelOf(RELATIONSHIP, recipient.relationship) }}</span>
        </template>
        님에게 과거에 준 선물을 기록해 두면, 다음 추천에서 중복·유사 후보를 피하는 데 활용됩니다.
      </p>

      <InlineAlert type="error" :message="error" />

      <div class="card card--pad addbox">
        <div class="input-row">
          <div class="field" style="flex: 2; margin: 0">
            <label class="field__label">선물 이름 *</label>
            <input class="input" v-model="form.giftName" placeholder="예: 텀블러" />
          </div>
          <div class="field" style="flex: 1; margin: 0">
            <label class="field__label">카테고리</label>
            <input class="input" v-model="form.giftCategory" placeholder="예: LIVING" />
          </div>
          <div class="field" style="flex: 1; margin: 0">
            <label class="field__label">준 날짜</label>
            <input class="input" type="date" v-model="form.giftedAt" />
          </div>
        </div>
        <div class="field" style="margin: 14px 0 0">
          <label class="field__label">메모 <span class="muted">(선택)</span></label>
          <input class="input" v-model="form.note" placeholder="예: 지난 생일에 선물" />
        </div>
        <div class="row-between" style="margin-top: 14px; justify-content: flex-end">
          <button class="btn btn--primary" :disabled="!form.giftName.trim() || saving" @click="add">
            <span class="material-symbols-outlined">add</span> {{ saving ? "저장 중..." : "과거 선물 추가" }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-block"><div class="spinner" /> 불러오는 중...</div>
      <InlineAlert v-else-if="loadError" type="error" :message="loadError" retry @retry="load" />
      <div v-else class="list">
        <div v-for="g in gifts" :key="g.previousGiftId" class="card card--pad row">
          <div>
            <strong>{{ g.giftName }}</strong>
            <span class="pill" v-if="g.giftCategory">{{ g.giftCategory }}</span>
            <div class="muted meta">
              {{ [g.giftedAt, g.note].filter(Boolean).join(" · ") || "기록된 상세 없음" }}
            </div>
          </div>
          <button class="btn--link btn--danger-link" @click="remove(g)">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
        <p v-if="!gifts.length" class="muted empty">등록된 과거 선물이 없습니다.</p>
      </div>

      <div class="actions">
        <button class="btn--link" @click="router.push({ name: 'SCR-RECIPIENT-001' })">← 대상 목록</button>
        <button
          class="btn btn--secondary"
          @click="router.push({ name: 'SCR-GIFT-001', params: { recipientId } })"
        >
          이 대상 추천 조건 입력 →
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.addbox {
  margin-bottom: 18px;
}
.addbox .btn .material-symbols-outlined {
  font-size: 16px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
}
.row strong {
  font-size: 14px;
  margin-right: 6px;
}
.meta {
  font-size: 12.5px;
  margin-top: 4px;
}
.row .material-symbols-outlined {
  font-size: 18px;
}
.empty {
  padding: 32px;
  text-align: center;
}
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
}
</style>
