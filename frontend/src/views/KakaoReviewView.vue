<script setup>
// SCR-KAKAO-002 · 추출 취향 단서 검토 · 승인 → 일괄 저장(PREF-002)
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useKakaoStore } from "../store/kakao";
import { useRecipientStore } from "../store/recipient";
import { PREFERENCE_TYPE, labelOf } from "../constants/enums";
import { extractApiError } from "../utils/apiError";

const router = useRouter();
const kakao = useKakaoStore();
const recipientStore = useRecipientStore();

const saving = ref(false);
const error = ref("");
const editingIdx = ref(null);
const editDraft = ref("");
const approvedCount = computed(() => kakao.items.filter((i) => i.approved).length);

onMounted(() => {
  if (!kakao.items.length) router.replace({ name: "SCR-KAKAO-001" });
});

const TYPE_ICON = {
  INTEREST: "interests",
  PREFERRED_CATEGORY: "category",
  PREFERRED_ATTRIBUTE: "palette",
  DISLIKED_CATEGORY: "block",
  WISH_ITEM: "redeem",
};

function startEdit(i, item) {
  editingIdx.value = i;
  editDraft.value = item.preferenceValue;
}
function commitEdit(item) {
  if (editDraft.value.trim()) item.preferenceValue = editDraft.value.trim();
  editingIdx.value = null;
}
function removeItem(i) {
  kakao.items.splice(i, 1);
}

async function save() {
  if (saving.value) return;
  saving.value = true;
  error.value = "";
  try {
    await kakao.saveApproved();
    const rid = recipientStore.selectedRecipientId;
    kakao.reset();
    router.push(
      rid ? { name: "SCR-GIFT-001", params: { recipientId: rid } } : { name: "SCR-RECIPIENT-001" }
    );
  } catch (e) {
    error.value = extractApiError(e, "취향 저장에 실패했습니다.").message;
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen screen--narrow">
      <p class="page-eyebrow">SCR-KAKAO-002 · UC6</p>
      <div class="row-between">
        <h1 class="page-title">추출된 취향 단서 검토</h1>
        <span class="pill pill--accent">{{ approvedCount }}건 승인됨</span>
      </div>
      <p class="page-desc">AI가 대화에서 추출한 단서를 확인·수정한 뒤, 추천에 사용할 항목만 승인합니다. 마음에 들지 않는 단서는 삭제하세요.</p>

      <InlineAlert type="error" :message="error" />

      <div class="card">
        <div v-for="(item, i) in kakao.items" :key="i" class="row">
          <span class="row__icon material-symbols-outlined">{{ TYPE_ICON[item.preferenceType] || "label" }}</span>
          <span class="pill row__type">{{ labelOf(PREFERENCE_TYPE, item.preferenceType) }}</span>

          <div class="row__val">
            <input
              v-if="editingIdx === i"
              class="input"
              v-model="editDraft"
              @keydown.enter.prevent="commitEdit(item)"
              @blur="commitEdit(item)"
            />
            <template v-else>{{ item.preferenceValue }}</template>
          </div>

          <label class="row__approve">
            <input type="checkbox" v-model="item.approved" />
            승인
          </label>
          <button class="btn--link" @click="startEdit(i, item)">
            <span class="material-symbols-outlined">edit</span>
          </button>
          <button class="btn--link btn--danger-link" @click="removeItem(i)">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
        <p v-if="!kakao.items.length" class="muted" style="padding: 24px; text-align: center">
          검토할 항목이 없습니다.
        </p>
      </div>

      <div class="submit">
        <span class="muted">체크한 항목만 KAKAO 소스로 저장되어 추천에 사용됩니다.</span>
        <button class="btn btn--primary" :disabled="saving" @click="save">
          <span class="material-symbols-outlined">check</span>
          {{ saving ? "저장 중..." : "승인하고 조건 입력으로" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
}
.row:last-child {
  border-bottom: 0;
}
.row__icon {
  font-size: 18px;
  color: var(--primary);
  flex-shrink: 0;
}
.row__type {
  flex-shrink: 0;
}
.row__val {
  flex: 1;
  font-size: 13.5px;
}
.row__approve {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  flex-shrink: 0;
}
.row .material-symbols-outlined {
  font-size: 17px;
}
.submit {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
}
.submit .muted {
  font-size: 13px;
}
.submit .btn .material-symbols-outlined {
  font-size: 16px;
}
@media (max-width: 640px) {
  .row {
    flex-wrap: wrap;
  }
  .row__val {
    flex-basis: 100%;
    order: 5;
  }
}
</style>
