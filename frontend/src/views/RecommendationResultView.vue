<script setup>
// SCR-AI-001 · 추천 결과 조회(UC8) + 후보별 좋아요/싫어요(UC9)
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useGiftStore } from "../store/gift";
import { OCCASION_TYPE, DISLIKE_REASON, toOptions, labelOf } from "../constants/enums";
import { giftImageUrl, giftImageFallbackUrl } from "../constants/giftImages";
import { extractApiError } from "../utils/apiError";

// candidateId -> 0: 로컬 이미지, 1: Unsplash 폴백, 2: 아이콘
const imgStage = ref({});
function candImg(c) {
  return (imgStage.value[c.candidateId] ?? 0) === 0
    ? giftImageUrl(c.giftName, c.giftCategory)
    : giftImageFallbackUrl(c.giftName, c.giftCategory);
}
function onImgError(c) {
  imgStage.value = {
    ...imgStage.value,
    [c.candidateId]: (imgStage.value[c.candidateId] ?? 0) + 1,
  };
}

const props = defineProps({ recommendationId: { type: String, required: true } });
const router = useRouter();
const giftStore = useGiftStore();

const REASONS = toOptions(DISLIKE_REASON);
const failed = ref(false);
const failMessage = ref("");
const actionError = ref("");
const busyCard = ref(null);
const dislikeFor = ref(null); // 사유 모달 대상 candidateId
const dislikeReason = ref("TASTE_MISMATCH");

const rec = computed(() => giftStore.recommendation);
const cond = computed(() => giftStore.condition);
const ready = computed(
  () =>
    rec.value &&
    (rec.value.status === "SUCCESS" || (rec.value.candidates?.length ?? 0) > 0)
);
const roundLabel = computed(() => (rec.value?.previousRecommendationId ? "재추천 결과" : "1차 추천"));
const sortedCandidates = computed(() =>
  [...(rec.value?.candidates ?? [])].sort(
    (a, b) => (a.recommendRank ?? 99) - (b.recommendRank ?? 99)
  )
);
const dislikedCount = computed(
  () => (rec.value?.candidates ?? []).filter((c) => c.feedback?.feedbackType === "DISLIKE").length
);

const won = (n) => (n ?? 0).toLocaleString("ko-KR");
const priceText = (c) =>
  c.estimatedPriceMin == null && c.estimatedPriceMax == null
    ? "가격 정보 없음"
    : c.estimatedPriceMin === c.estimatedPriceMax
      ? `${won(c.estimatedPriceMax)}원`
      : `${won(c.estimatedPriceMin)} ~ ${won(c.estimatedPriceMax)}원`;

function budgetClass(c) {
  const b = cond.value;
  if (!b?.budgetMax || c.estimatedPriceMax == null) return "";
  if (c.estimatedPriceMax <= b.budgetMax && c.estimatedPriceMin >= (b.budgetMin ?? 0)) return "pill--accent";
  if (c.estimatedPriceMax < (b.budgetMin ?? 0)) return "pill--info";
  if (c.estimatedPriceMin > b.budgetMax) return "pill--danger";
  return "pill--warning";
}
const budgetLabel = (c) =>
  ({ "pill--accent": "예산 내", "pill--info": "예산보다 낮음", "pill--danger": "예산 초과", "pill--warning": "예산 근접" }[
    budgetClass(c)
  ] || "예상가");

async function loadResult() {
  failed.value = false;
  failMessage.value = "";
  try {
    await giftStore.loadRecommendation(props.recommendationId);
    if (giftStore.recommendation?.status === "FAILED") {
      failed.value = true;
      failMessage.value =
        giftStore.recommendation?.failure?.message ||
        "추천 생성에 실패했습니다. 조건을 조정해 다시 시도해 주세요.";
    }
  } catch (e) {
    failed.value = true;
    failMessage.value = extractApiError(
      e,
      "추천 결과를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
    ).message;
  }
}
onMounted(loadResult);

const selectedCandidate = computed(
  () => (rec.value?.candidates ?? []).find((c) => c.selectedAt) || null
);

async function like(c) {
  actionError.value = "";
  busyCard.value = c.candidateId;
  try {
    if (c.feedback?.feedbackType !== "LIKE") {
      await giftStore.sendFeedback(c.candidateId, { feedbackType: "LIKE" });
    }
  } catch (e) {
    actionError.value = extractApiError(e, "피드백 등록에 실패했습니다.").message;
  } finally {
    busyCard.value = null;
  }
}
async function toggleSelect(c) {
  actionError.value = "";
  busyCard.value = c.candidateId;
  try {
    if (c.selectedAt) await giftStore.deselectCandidate(c.candidateId);
    else await giftStore.selectCandidate(c.candidateId);
  } catch (e) {
    actionError.value = extractApiError(e, "선택 처리에 실패했습니다.").message;
  } finally {
    busyCard.value = null;
  }
}
function openDislike(c) {
  dislikeFor.value = c.candidateId;
  dislikeReason.value = c.feedback?.dislikeReason || "TASTE_MISMATCH";
}
async function confirmDislike() {
  const id = dislikeFor.value;
  actionError.value = "";
  busyCard.value = id;
  try {
    await giftStore.sendFeedback(id, { feedbackType: "DISLIKE", dislikeReason: dislikeReason.value });
    dislikeFor.value = null;
  } catch (e) {
    actionError.value = extractApiError(e, "피드백 등록에 실패했습니다.").message;
  } finally {
    busyCard.value = null;
  }
}
function goReRecommend() {
  router.push({ name: "SCR-AI-002", params: { recommendationId: props.recommendationId } });
}
</script>

<template>
  <div>
    <AppNav />
    <div class="screen">
      <div v-if="failed" class="loading-block">
        <span class="material-symbols-outlined" style="font-size: 32px; color: var(--danger)">error</span>
        <span>{{ failMessage }}</span>
        <div style="display: flex; gap: 8px">
          <button class="btn btn--outline btn--sm" @click="loadResult">다시 시도</button>
          <button class="btn btn--ghost btn--sm" @click="router.push({ name: 'SCR-RECIPIENT-001' })">
            대상 목록으로
          </button>
        </div>
      </div>

      <div v-else-if="!ready" class="loading-block">
        <div class="spinner" />
        추천 결과를 불러오는 중...
      </div>

      <template v-else>
        <InlineAlert type="error" :message="actionError" />
        <div class="head">
          <div>
            <div class="head__label">
              <span class="pill pill--ai"><span class="material-symbols-outlined">auto_awesome</span> AI RECOMMENDATION</span>
              <span class="muted" style="font-size: 12px">원문 미저장 비식별 모드</span>
            </div>
            <h1 class="page-title">AI 종합 선물 추천 후보 <span class="accent">({{ roundLabel }})</span></h1>
            <p class="page-desc" style="margin-bottom: 0">
              단순 인기 랭킹이 아닌, 분석·승인된 대화 맥락과 예산 프로필을 기반으로 도출된 고유 후보입니다.
            </p>
          </div>
          <div class="metrics card card--pad" v-if="cond">
            <div>
              <span class="metrics__k">후보 수</span>
              <div class="metrics__v">{{ rec.candidates.length }}<span>개</span></div>
            </div>
            <div class="metrics__div" />
            <div>
              <span class="metrics__k">반영 예산</span>
              <div class="metrics__v metrics__v--sm">
                {{ cond.budgetMin != null ? won(cond.budgetMin) + "~" + won(cond.budgetMax) + "원" : "미지정" }}
              </div>
            </div>
          </div>
        </div>

        <div class="ribbon card" v-if="cond">
          <span class="ribbon__label"><span class="material-symbols-outlined">tune</span> 적용 조건</span>
          <span class="chip chip--static">목적: {{ labelOf(OCCASION_TYPE, cond.occasionType) }}</span>
          <span class="chip chip--static" v-if="cond.budgetMin != null">예산 {{ won(cond.budgetMin) }}~{{ won(cond.budgetMax) }}원</span>
          <span class="chip chip--static" v-if="cond.preferenceNote">취향: {{ cond.preferenceNote }}</span>
          <span class="chip chip--static" v-if="cond.avoidGiftNote">제외: {{ cond.avoidGiftNote }}</span>
        </div>

        <p class="section-label">큐레이션 추천 후보 (Top {{ rec.candidates.length }})</p>
        <InlineAlert
          v-if="selectedCandidate"
          type="success"
          :message="`'${selectedCandidate.giftName}' 을(를) 최종 선물로 선택했습니다.`"
        />
        <div class="cands">
          <article
            v-for="c in sortedCandidates"
            :key="c.candidateId"
            class="card cand"
            :class="{
              'cand--like': c.feedback?.feedbackType === 'LIKE',
              'cand--dislike': c.feedback?.feedbackType === 'DISLIKE',
              'cand--selected': !!c.selectedAt,
            }"
          >
            <div class="cand__media">
              <img
                v-if="(imgStage[c.candidateId] ?? 0) < 2"
                :src="candImg(c)"
                :alt="c.giftName"
                loading="lazy"
                @error="onImgError(c)"
              />
              <span v-else class="material-symbols-outlined">redeem</span>
              <span class="cand__rank">후보 {{ String(c.recommendRank ?? 0).padStart(2, "0") }}</span>
              <span class="cand__cat" v-if="c.giftCategory">{{ c.giftCategory }}</span>
            </div>

            <div class="cand__metaRow">
              <span class="pill" :class="budgetClass(c)">{{ budgetLabel(c) }}</span>
              <span class="cand__price">{{ priceText(c) }}</span>
            </div>
            <h3 class="cand__name">{{ c.giftName }}</h3>

            <div class="cand__reason">
              <div class="cand__reason-h"><span class="material-symbols-outlined">psychology</span> AI 큐레이션 선정 이유</div>
              <p>{{ c.recommendationReason }}</p>
            </div>
            <div class="cand__caution" v-if="c.cautionNote">
              <span class="material-symbols-outlined">tips_and_updates</span>
              <div><strong>구매 전 살짝 확인하기</strong><p>{{ c.cautionNote }}</p></div>
            </div>

            <div class="cand__fbState" v-if="c.feedback || c.selectedAt">
              <span v-if="c.selectedAt" class="pill pill--accent">
                <span class="material-symbols-outlined">check</span> 최종 선택됨
              </span>
              <span v-if="c.feedback?.feedbackType === 'LIKE'" class="pill pill--info">좋아요</span>
              <span v-else-if="c.feedback?.feedbackType === 'DISLIKE'" class="pill pill--danger">
                싫어요 · {{ labelOf(DISLIKE_REASON, c.feedback.dislikeReason) }}
              </span>
            </div>

            <div class="cand__acts">
              <button
                class="btn btn--sm"
                :class="c.feedback?.feedbackType === 'LIKE' ? 'btn--secondary' : 'btn--outline'"
                :disabled="busyCard === c.candidateId"
                @click="like(c)"
              >
                <span class="material-symbols-outlined">favorite</span> 좋아요
              </button>
              <button
                class="btn btn--sm"
                :class="c.feedback?.feedbackType === 'DISLIKE' ? 'btn--danger' : 'btn--outline'"
                :disabled="busyCard === c.candidateId"
                @click="openDislike(c)"
              >
                <span class="material-symbols-outlined">thumb_down</span> 싫어요
              </button>
            </div>
            <button
              class="btn btn--sm btn--block cand__select"
              :class="c.selectedAt ? 'btn--secondary' : 'btn--primary'"
              :disabled="busyCard === c.candidateId"
              @click="toggleSelect(c)"
            >
              <span class="material-symbols-outlined">{{ c.selectedAt ? "close" : "redeem" }}</span>
              {{ c.selectedAt ? "선택 취소" : "최종 선물로 선택" }}
            </button>
          </article>
        </div>

        <div class="card card--pad rerec">
          <div>
            <strong>마음에 드는 후보가 없으신가요?</strong>
            <p class="muted">싫어요 사유를 남긴 뒤 재추천을 요청하면 같은 조건으로 새 후보를 생성합니다. (현재 싫어요 {{ dislikedCount }}건)</p>
          </div>
          <button class="btn btn--dark" @click="goReRecommend">
            <span class="material-symbols-outlined">refresh</span> 재추천 진행
          </button>
        </div>
      </template>
    </div>

    <div v-if="dislikeFor" class="modal-backdrop" @click.self="dislikeFor = null">
      <div class="modal">
        <h3 style="font-size: 17px; margin-bottom: 8px">싫어요 사유</h3>
        <p class="muted" style="font-size: 13px; margin-bottom: 16px">
          이 후보가 맞지 않은 이유를 하나 선택해 주세요. 재추천에서 걸러냅니다.
        </p>
        <div class="reasons">
          <label v-for="r in REASONS" :key="r.value" class="reason">
            <input type="radio" name="dislikeReason" :value="r.value" v-model="dislikeReason" />
            {{ r.label }}
          </label>
        </div>
        <div class="row-between" style="margin-top: 18px; justify-content: flex-end; gap: 10px">
          <button class="btn--link" @click="dislikeFor = null">취소</button>
          <button class="btn btn--danger btn--sm" :disabled="busyCard" @click="confirmDislike">싫어요 등록</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}
.head__label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.head__label .material-symbols-outlined {
  font-size: 14px;
}
.accent {
  color: var(--danger);
}
.metrics {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  flex-shrink: 0;
}
.metrics__k {
  font-family: var(--font-label);
  font-size: 11px;
  color: var(--text-muted);
}
.metrics__v {
  font-family: var(--font-label);
  font-size: 22px;
  font-weight: 800;
}
.metrics__v span {
  font-size: 11px;
  color: var(--text-muted);
  margin-left: 2px;
}
.metrics__v--sm {
  font-size: 14px;
}
.metrics__div {
  width: 1px;
  align-self: stretch;
  background: var(--border);
}
.ribbon {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  margin-top: 18px;
}
.ribbon__label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-label);
  font-size: 12px;
  font-weight: 700;
}
.ribbon__label .material-symbols-outlined {
  font-size: 15px;
  color: var(--primary);
}
.cands {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}
.cand {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cand--like {
  border-color: var(--primary);
}
.cand--dislike {
  border-color: var(--danger);
  opacity: 0.72;
}
.cand--selected {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px var(--primary-soft);
  opacity: 1;
}
.cand__fbState .material-symbols-outlined {
  font-size: 13px;
}
.cand__media {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--primary-soft), #fff3ea);
  border: 1px solid var(--border);
  display: grid;
  place-items: center;
  overflow: hidden;
}
.cand__media > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cand__media > .material-symbols-outlined {
  font-size: 44px;
  color: var(--primary);
  opacity: 0.65;
}
.cand__rank,
.cand__cat {
  position: absolute;
  font-family: var(--font-label);
  font-size: 10.5px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 2px 7px;
}
.cand__rank {
  top: 8px;
  left: 8px;
}
.cand__cat {
  bottom: 8px;
  left: 8px;
  color: var(--text-muted);
}
.cand__metaRow {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}
.cand__price {
  font-family: var(--font-label);
  font-size: 16px;
  font-weight: 800;
}
.cand__name {
  font-size: 15px;
  line-height: 1.4;
}
.cand__reason {
  background: var(--primary-soft);
  border: 1px solid var(--primary-border);
  border-radius: var(--radius);
  padding: 12px;
}
.cand__reason-h {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-label);
  font-size: 11.5px;
  font-weight: 700;
  color: #a94f00;
  margin-bottom: 4px;
}
.cand__reason-h .material-symbols-outlined {
  font-size: 15px;
}
.cand__reason p {
  font-size: 12.5px;
  line-height: 1.6;
}
.cand__caution {
  display: flex;
  gap: 8px;
  background: var(--surface-tint);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 10px 12px;
}
.cand__caution .material-symbols-outlined {
  font-size: 17px;
  color: var(--text-muted);
  flex-shrink: 0;
}
.cand__caution strong {
  font-size: 12px;
}
.cand__caution p {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}
.cand__fbState {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}
.cand__acts {
  display: flex;
  gap: 8px;
  margin-top: auto;
}
.cand__acts .btn {
  flex: 1;
}
.cand__acts .material-symbols-outlined {
  font-size: 15px;
}
.cand__select .material-symbols-outlined {
  font-size: 15px;
}
.rerec {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
}
.rerec p {
  font-size: 12.5px;
  margin-top: 3px;
}
.rerec .btn .material-symbols-outlined {
  font-size: 16px;
}
.reasons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.reason {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
}
@media (max-width: 900px) {
  .head {
    flex-direction: column;
    align-items: flex-start;
  }
  .cands {
    grid-template-columns: 1fr;
  }
  .reasons {
    grid-template-columns: 1fr;
  }
}
</style>
