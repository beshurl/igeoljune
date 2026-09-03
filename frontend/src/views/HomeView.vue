<script setup>
// SCR-HOME-001 · 서비스 소개 랜딩 (비로그인 진입)
import { useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";

const router = useRouter();
const auth = useAuthStore();

function start() {
  router.push({ name: auth.isLoggedIn ? "SCR-RECIPIENT-001" : "SCR-AUTH-002" });
}
function goLogin() {
  router.push({ name: "SCR-AUTH-001" });
}

const features = [
  { icon: "lock", title: "비소셜 1인용 안심 도구", desc: "친구 추가나 상대방 조회 없이, 내 계정에서만 보이는 비공개 큐레이션 공간입니다." },
  { icon: "forum", title: "카톡 대화 취향 임시 추출", desc: "대화 내보내기 파일에서 선물 관련 취향 단서만 뽑아내고 원문은 분석 즉시 파기합니다." },
  { icon: "psychology", title: "근거 있는 추천 & 솔직 피드백", desc: "상품명만이 아니라 반영한 조건과 이유를 함께 제시하고, 싫어요 사유로 다시 추천합니다." },
];
const steps = [
  { n: 1, t: "회원가입 · 로그인", d: "이메일로 개인 데이터 영역에 진입" },
  { n: 2, t: "대상 · 취향 · 과거 선물 관리", d: "관계·연령대·취향 메모와 과거에 준 선물 기록" },
  { n: 3, t: "조건 입력 → AI 추천", d: "예산·목적·제외 조건 입력 후 여러 후보와 이유 확인" },
  { n: 4, t: "피드백 → 재추천", d: "후보별 좋아요·싫어요, 사유를 반영해 새 후보 생성" },
];
const compare = [
  ["추천 기준", "단순 판매량·광고·제휴 상품 중심", "받는 사람의 실제 맥락·취향 분석"],
  ["추천 이유", "\"베스트\" 배지만 표시", "\"인테리어 취향과 생일 상황에 부합\" 처럼 설명"],
  ["프라이버시", "소셜 피드·공유 기본", "1인용 비공개, 카톡 원문 분석 즉시 파기"],
  ["피드백 반영", "카테고리 광고 재노출", "싫어요 사유 반영 재추천"],
];
</script>

<template>
  <div class="home">
    <AppNav />

    <section class="hero">
      <div class="glow" />
      <div class="hero__text">
        <span class="pill pill--ai"><span class="material-symbols-outlined">auto_awesome</span> PRIVACY-FIRST GIFT AI</span>
        <h1 class="hero__title">
          받는 사람도 주는 사람도<br />
          감동하는 <span class="accent">실패 없는 선물</span>
        </h1>
        <p class="hero__sub">
          연령·관계뿐 아니라 카카오톡 대화 속 관심 키워드까지 종합 분석해
          '진짜 맞춤형 선물'과 명확한 추천 이유를 찾아드립니다.
        </p>
        <div class="hero__cta">
          <button class="btn btn--primary btn--lg" @click="start">
            <span class="material-symbols-outlined">redeem</span> 선물 추천 시작하기
          </button>
          <button class="btn btn--outline btn--lg" @click="goLogin">이미 계정이 있어요</button>
        </div>
        <p class="hero__note">
          <span class="material-symbols-outlined">verified_user</span>
          별도의 민감한 개인정보를 요구하지 않으며, 카톡 원문은 저장되지 않습니다.
        </p>
      </div>
      <div class="hero__art">
        <div class="hero__card card">
          <div class="hero__card-media"><span class="material-symbols-outlined">redeem</span></div>
          <div class="hero__card-body">
            <span class="pill pill--accent">20대 후반 절친 · 생일 선물</span>
            <strong>하리오 V60 드립 세트</strong>
            <p class="muted">"홈카페 취향과 예산 5~7만원, 부담 없는 관계"를 함께 반영</p>
          </div>
        </div>
      </div>
    </section>

    <section class="block">
      <p class="page-eyebrow" style="text-align: center">PRIVACY &amp; ACCURACY FIRST</p>
      <h2 class="block__title">선물 고민의 스트레스는 덜고,<br />보내는 마음의 순도는 지킵니다</h2>
      <div class="feats">
        <article v-for="f in features" :key="f.title" class="card card--pad feat">
          <span class="feat__icon material-symbols-outlined">{{ f.icon }}</span>
          <strong>{{ f.title }}</strong>
          <p class="muted">{{ f.desc }}</p>
        </article>
      </div>
    </section>

    <section class="block">
      <h2 class="block__title">어떻게 추천이 진행되나요?</h2>
      <div class="steps">
        <article v-for="s in steps" :key="s.n" class="card card--pad step">
          <span class="step__n">{{ s.n }}</span>
          <strong>{{ s.t }}</strong>
          <p class="muted">{{ s.d }}</p>
        </article>
      </div>
    </section>

    <section class="block">
      <h2 class="block__title">일반 쇼핑몰 랭킹 검색과 무엇이 다른가요?</h2>
      <div class="card cmp">
        <div class="cmp__row cmp__row--head"><span>구분</span><span>일반 쇼핑몰</span><span class="accent">이걸주네?</span></div>
        <div v-for="row in compare" :key="row[0]" class="cmp__row">
          <span>{{ row[0] }}</span><span class="muted">{{ row[1] }}</span><span>{{ row[2] }}</span>
        </div>
      </div>
    </section>

    <section class="cta">
      <span class="cta__bot">🎁</span>
      <div>
        <strong class="cta__title">이제 고마운 마음을 온전히 전하는 데만 집중하세요</strong>
        <p class="muted">소중한 사람에게 딱 맞는 선물, 불필요한 개인정보 유출 없이 지금 시작해 보세요.</p>
      </div>
      <button class="btn btn--primary btn--lg" @click="start">무료로 시작하기</button>
    </section>

    <footer class="hfoot">
      <span class="material-symbols-outlined">verified_user</span>
      카카오톡 대화 원문은 분석 즉시 파기되며 다른 이용자에게 공유되지 않습니다.
      <span class="hfoot__c">© 2026 이걸주네? · Privacy-First Gift Intelligence</span>
    </footer>
  </div>
</template>

<style scoped>
.home {
  min-height: 100%;
}
.accent {
  color: var(--primary);
}
.hero {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 64px 32px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
  overflow: hidden;
}
.glow {
  position: absolute;
  top: -120px;
  right: -60px;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 122, 0, 0.1);
  filter: blur(90px);
  pointer-events: none;
}
.hero__title {
  font-size: 40px;
  line-height: 1.3;
  letter-spacing: -0.03em;
  margin: 16px 0;
}
.hero__sub {
  color: var(--text-muted);
  font-size: 15px;
  max-width: 46ch;
}
.hero__cta {
  display: flex;
  gap: 12px;
  margin: 24px 0 16px;
  flex-wrap: wrap;
}
.hero__cta .material-symbols-outlined {
  font-size: 18px;
}
.hero__note {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-faint);
}
.hero__note .material-symbols-outlined {
  font-size: 15px;
  color: var(--primary);
}
.hero__art {
  display: grid;
  place-items: center;
}
.hero__card {
  width: 300px;
  padding: 16px;
  transform: rotate(2deg);
}
.hero__card-media {
  aspect-ratio: 4 / 3;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--primary-soft), #fff3ea);
  display: grid;
  place-items: center;
  margin-bottom: 12px;
}
.hero__card-media .material-symbols-outlined {
  font-size: 48px;
  color: var(--primary);
  opacity: 0.7;
}
.hero__card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.hero__card-body strong {
  font-size: 14px;
}
.hero__card-body p {
  font-size: 12px;
}
.block {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 32px 0;
}
.block__title {
  font-size: 22px;
  text-align: center;
  line-height: 1.4;
  margin-bottom: 28px;
}
.feats,
.steps {
  display: grid;
  gap: 18px;
}
.feats {
  grid-template-columns: repeat(3, 1fr);
}
.steps {
  grid-template-columns: repeat(4, 1fr);
}
.feat__icon {
  font-size: 26px;
  color: var(--primary);
  margin-bottom: 10px;
}
.feat strong,
.step strong {
  display: block;
  margin-bottom: 6px;
  font-size: 15px;
}
.feat p,
.step p {
  font-size: 12.5px;
  line-height: 1.6;
}
.step__n {
  display: inline-grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary);
  font-family: var(--font-label);
  font-weight: 800;
  font-size: 12px;
  margin-bottom: 10px;
}
.cmp {
  overflow: hidden;
}
.cmp__row {
  display: grid;
  grid-template-columns: 130px 1fr 1fr;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.cmp__row:last-child {
  border-bottom: 0;
}
.cmp__row--head {
  background: var(--surface-tint);
  font-family: var(--font-label);
  font-weight: 700;
}
.cta {
  max-width: 1200px;
  margin: 56px auto 40px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--primary-soft);
  border: 1px solid var(--primary-border);
  border-radius: var(--radius-xl);
}
.cta__bot {
  font-size: 44px;
}
.cta__title {
  font-size: 18px;
}
.cta div {
  flex: 1;
}
.cta p {
  font-size: 13px;
  margin-top: 4px;
}
.hfoot {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 32px 48px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: var(--text-muted);
  border-top: 1px solid var(--border);
}
.hfoot .material-symbols-outlined {
  font-size: 16px;
  color: var(--primary);
}
.hfoot__c {
  margin-left: auto;
  font-family: var(--font-label);
  font-size: 11px;
}
@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .hero__art {
    display: none;
  }
  .feats,
  .steps {
    grid-template-columns: 1fr 1fr;
  }
}
@media (max-width: 560px) {
  .feats,
  .steps {
    grid-template-columns: 1fr;
  }
  .cmp__row {
    grid-template-columns: 1fr;
  }
  .cta {
    flex-direction: column;
    text-align: center;
  }
}
</style>
