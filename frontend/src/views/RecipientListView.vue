<script setup>
// SCR-RECIPIENT-001 · 선물 대상 관리 (등록·조회·선택·수정·삭제)
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRecipientStore } from "../store/recipient";
import { RELATIONSHIP, AGE_GROUP, GENDER, toOptions, labelOf } from "../constants/enums";

const router = useRouter();
const store = useRecipientStore();

const RELATION_OPTS = toOptions(RELATIONSHIP);
const AGE_OPTS = toOptions(AGE_GROUP);
const GENDER_OPTS = toOptions(GENDER);

const blank = () => ({
  name: "",
  relationship: "FRIEND",
  ageGroup: "LATE_20S",
  gender: "FEMALE",
  job: "",
});
const form = reactive(blank());
const editingId = ref(null);
const saving = ref(false);
const filter = ref("ALL");

const count = computed(() => store.recipients.length);
const shown = computed(() =>
  filter.value === "ALL"
    ? store.recipients
    : store.recipients.filter((r) => r.relationship === filter.value)
);

onMounted(() => store.loadRecipients());

function metaOf(r) {
  return [labelOf(AGE_GROUP, r.ageGroup), labelOf(GENDER, r.gender), r.job]
    .filter(Boolean)
    .join(" · ");
}
function editRow(r) {
  editingId.value = r.recipientId;
  Object.assign(form, {
    name: r.name,
    relationship: r.relationship,
    ageGroup: r.ageGroup,
    gender: r.gender,
    job: r.job || "",
  });
}
function cancelEdit() {
  editingId.value = null;
  Object.assign(form, blank());
}
async function save() {
  if (!form.name.trim() || saving.value) return;
  saving.value = true;
  try {
    if (editingId.value) await store.editRecipient(editingId.value, { ...form });
    else await store.addRecipient({ ...form });
    cancelEdit();
  } finally {
    saving.value = false;
  }
}
async function remove(r) {
  if (confirm(`'${r.name}' 대상을 삭제할까요?`)) await store.removeRecipient(r.recipientId);
}
function recommend(r) {
  store.select(r.recipientId);
  router.push({ name: "SCR-GIFT-001", params: { recipientId: r.recipientId } });
}
function openHistory(r) {
  store.select(r.recipientId);
  router.push({ name: "SCR-HISTORY-001", params: { recipientId: r.recipientId } });
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen">
      <p class="page-eyebrow">SCR-RECIPIENT-001 · UC2</p>
      <div class="head">
        <div>
          <h1 class="page-title">선물 대상 관리</h1>
          <p class="page-desc">
            선물을 전할 소중한 사람들의 취향과 기본 정보를 비공개로 관리하세요.
            상대방에겐 알림이 가지 않으며 오직 당신의 추천에만 반영됩니다.
          </p>
        </div>
        <div class="stat card card--pad">
          <span class="material-symbols-outlined">group</span>
          <div><strong>{{ count }}</strong>명 등록</div>
        </div>
      </div>

      <div class="filters">
        <button class="chip" :class="{ 'chip--on': filter === 'ALL' }" @click="filter = 'ALL'">
          전체 ({{ count }})
        </button>
        <button
          v-for="o in RELATION_OPTS"
          :key="o.value"
          class="chip"
          :class="{ 'chip--on': filter === o.value }"
          @click="filter = o.value"
        >
          {{ o.label }}
        </button>
      </div>

      <div class="cols">
        <div class="list">
          <article
            v-for="r in shown"
            :key="r.recipientId"
            class="card card--pad recipient"
            :class="{ 'recipient--on': store.selectedRecipientId === r.recipientId }"
          >
            <div class="recipient__top">
              <span class="recipient__avatar">{{ r.name.slice(0, 1) }}</span>
              <div class="recipient__id">
                <div class="recipient__name">
                  {{ r.name }}
                  <span class="pill pill--accent">{{ labelOf(RELATIONSHIP, r.relationship) }}</span>
                  <span v-if="store.selectedRecipientId === r.recipientId" class="pill pill--success">
                    선택됨
                  </span>
                </div>
                <div class="recipient__meta">{{ metaOf(r) || "-" }}</div>
              </div>
              <div class="recipient__acts">
                <button class="btn--link" @click="editRow(r)">
                  <span class="material-symbols-outlined">edit</span>
                </button>
                <button class="btn--link btn--danger-link" @click="remove(r)">
                  <span class="material-symbols-outlined">delete</span>
                </button>
              </div>
            </div>
            <div class="recipient__cta">
              <button class="btn btn--secondary btn--sm" @click="recommend(r)">
                <span class="material-symbols-outlined">auto_awesome</span>
                맞춤 선물 추천받기
              </button>
              <button class="btn btn--outline btn--sm" @click="openHistory(r)">
                <span class="material-symbols-outlined">history</span>
                과거 선물
              </button>
            </div>
          </article>

          <p v-if="!shown.length" class="muted empty">해당하는 대상이 없습니다.</p>
        </div>

        <aside class="card card--pad form">
          <div class="form__head">
            <span class="material-symbols-outlined">person_add</span>
            <div>
              <strong>{{ editingId ? "대상 정보 수정" : "새 대상 등록" }}</strong>
              <p class="muted">소중한 인연의 취향을 기록하세요</p>
            </div>
          </div>

          <div class="field">
            <label class="field__label">이름 또는 별칭 *</label>
            <input class="input" v-model="form.name" placeholder="예: 지민, 박부장님, 우리형" />
          </div>
          <div class="field">
            <label class="field__label">관계 선택 *</label>
            <div class="chips">
              <button
                v-for="o in RELATION_OPTS"
                :key="o.value"
                class="chip"
                :class="{ 'chip--on': form.relationship === o.value }"
                @click="form.relationship = o.value"
              >
                {{ o.label }}
              </button>
            </div>
          </div>
          <div class="input-row">
            <div class="field" style="flex: 1">
              <label class="field__label">연령대</label>
              <select class="select" v-model="form.ageGroup">
                <option v-for="o in AGE_OPTS" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>
            </div>
            <div class="field" style="flex: 1">
              <label class="field__label">성별</label>
              <select class="select" v-model="form.gender">
                <option v-for="o in GENDER_OPTS" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>
            </div>
          </div>
          <div class="field">
            <label class="field__label">직업 / 라이프스타일 <span class="muted">(선택)</span></label>
            <input class="input" v-model="form.job" placeholder="예: 프리랜서 디자이너, 대학원생" />
          </div>

          <div class="note">
            <span class="material-symbols-outlined">lock</span>
            등록된 상대에게 카카오톡 메시지나 알림이 전혀 발송되지 않으며, 본인의 추천에만 사용됩니다.
          </div>

          <div class="form__actions">
            <button v-if="editingId" class="btn--link" @click="cancelEdit">취소</button>
            <button class="btn btn--primary btn--block" :disabled="!form.name.trim() || saving" @click="save">
              <span class="material-symbols-outlined">check</span>
              {{ saving ? "저장 중..." : editingId ? "수정 완료" : "대상 등록 완료하기" }}
            </button>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}
.stat {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
}
.stat strong {
  font-family: var(--font-label);
  font-size: 18px;
  color: var(--text);
}
.stat .material-symbols-outlined {
  color: var(--primary);
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 18px 0 22px;
}
.cols {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.recipient--on {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-soft);
}
.recipient__top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.recipient__avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary);
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 17px;
  flex-shrink: 0;
}
.recipient__id {
  flex: 1;
  min-width: 0;
}
.recipient__name {
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}
.recipient__meta {
  font-size: 12.5px;
  color: var(--text-muted);
  margin-top: 3px;
}
.recipient__acts {
  display: flex;
  gap: 2px;
}
.recipient__acts .material-symbols-outlined {
  font-size: 18px;
}
.recipient__cta {
  margin-top: 14px;
  display: flex;
  gap: 8px;
}
.recipient__cta .btn:first-child {
  flex: 1;
}
.recipient__cta .material-symbols-outlined {
  font-size: 15px;
}
.empty {
  padding: 40px;
  text-align: center;
}
.form {
  position: sticky;
  top: 88px;
}
.form__head {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 18px;
}
.form__head .material-symbols-outlined {
  color: var(--primary);
}
.form__head p {
  font-size: 12px;
  margin: 2px 0 0;
}
.note {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  line-height: 1.5;
}
.note .material-symbols-outlined {
  font-size: 16px;
  color: var(--primary);
  flex-shrink: 0;
}
.form__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
}
.form__actions .btn .material-symbols-outlined {
  font-size: 16px;
}
@media (max-width: 980px) {
  .cols {
    grid-template-columns: 1fr;
  }
  .form {
    position: static;
  }
}
</style>
