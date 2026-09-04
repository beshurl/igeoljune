// 선물 이미지. giftName 으로 개별 매핑한다.
// 1순위: public/gift/ 의 로컬 상품 이미지 (LOCAL)
// 2순위: Unsplash 이름 매핑 (BY_NAME) → 카테고리(BY_CATEGORY) → 선물상자 fallback
// 이름 목록은 백엔드 GiftCatalog 기준.
const BASE = "https://images.unsplash.com/photo-";
const PARAMS = "?w=640&h=480&fit=crop&crop=entropy&auto=format&q=70";

// 직접 제공한 상품 이미지 (frontend/public/gift/ 에 아래 파일명으로 저장)
const LOCAL = {
  "핸드드립 커피 세트": "handdrip-coffee.jpg",
  "에스프레소 캡슐 머신": "espresso-machine.jpg",
  "보온 텀블러": "tumbler.jpg",
  "무선 충전 거치대": "wireless-charger.jpg",
  "캐시미어 머플러": "cashmere-scarf.jpg",
  "가죽 카드지갑": "leather-cardwallet.jpg",
  "프리미엄 입욕제 세트": "bath-salt-set.jpg",
  "핸드크림 기프트 세트": "handcream-set.jpg",
  "홍삼 스틱 선물세트": "red-ginseng.jpg",
  "마사지 건": "massage-gun.jpg",
  "원목 데스크 정리함": "desk-organizer.jpg",
  "전자책 리더기": "ereader.png",
};

// 상품명 → Unsplash photo id (전부 200 확인)
const BY_NAME = {
  "핸드드립 커피 세트": "1442550528053-c431ecb55509",
  "에스프레소 캡슐 머신": "1607681034540-2c46cc71896d",
  "스페셜티 원두 구독권 3개월": "1559056199-641a0ac8b55e",
  "보온 텀블러": "1610824352934-c10d87b700cc",
  "무선 충전 거치대": "1600490722773-35753aea6332",
  "기계식 키보드": "1587829741301-dc798b83add3",
  "노이즈 캔슬링 이어폰": "1590658268037-6bf12165a8df",
  "러닝화": "1542291026-7eec264c27ff",
  "러닝 웨어 세트": "1483721310020-03333e577078",
  "스마트 워치 스트랩": "1579586337278-3befd40fd17a",
  "캐시미어 머플러": "1520903920243-00d872a2d1c9",
  "가죽 카드지갑": "1553062407-98eeb64c6a62",
  "아로마 디퓨저 세트": "1608571423902-eed4a5ad8108",
  "무드등 조명": "1513506003901-1e6a229e2d15",
  "호텔식 침구 세트": "1522771739844-6a9f6d5f14af",
  "프리미엄 입욕제 세트": "1600428610161-e98636332e98",
  "핸드크림 기프트 세트": "1611930022073-b7a4ba5fcccd",
  "홍삼 스틱 선물세트": "1550572017-edd951b55104",
  "마사지 건": "1746278925416-9d6c71f55c2d",
  "디저트 케이크 기프트카드": "1578985545062-69928b1d9587",
  "수제 과일청 세트": "1474979266404-7eaacbcd87c5",
  "원목 데스크 정리함": "1587467512961-120760940315",
  "드로잉 태블릿": "1611241893603-3c359704e0ee",
  "보드게임 세트": "1610890716171-6b1bb98ffd09",
  "베스트셀러 에세이 3권 세트": "1512820790803-83ca734da794",
  "전자책 리더기": "1592434134753-a70baf7979d5",
  "반려동물 간식 선물세트": "1583511655857-d19b40a7a54e",
  "만년필 & 잉크 세트": "1585336261022-680e295ce3fe",
  "원데이 클래스 이용권": "1556910103-1c02745aae4d",
  "호캉스 다이닝 이용권": "1414235077428-338989a2e8c0",
};

// 카탈로그에 없는 신규 상품용 카테고리 fallback
const BY_CATEGORY = {
  HOME_CAFE: "1495474472287-4d71bcdd2085",
  LIVING: "1586023492125-27b2c045efd7",
  FASHION: "1542291026-7eec264c27ff",
  EXPERIENCE: "1414235077428-338989a2e8c0",
  HEALTH: "1620916566398-39f1143ab7be",
  TECH: "1587829741301-dc798b83add3",
  FOOD: "1578985545062-69928b1d9587",
  BOOK: "1512820790803-83ca734da794",
  BEAUTY: "1556228578-8c89e6adf883",
  PET: "1583511655857-d19b40a7a54e",
  STATIONERY: "1585336261022-680e295ce3fe",
  HOBBY: "1610890716171-6b1bb98ffd09",
};
const FALLBACK = "1513885535751-8b9238bd345a"; // 선물 상자

export function giftImageUrl(giftName, category) {
  const name = giftName?.trim();
  if (LOCAL[name]) return `/gift/${LOCAL[name]}`;
  const id = BY_NAME[name] || BY_CATEGORY[category] || FALLBACK;
  return BASE + id + PARAMS;
}

// 로컬 이미지 로드 실패 시 폴백으로 쓸 Unsplash URL
export function giftImageFallbackUrl(giftName, category) {
  const id = BY_NAME[giftName?.trim()] || BY_CATEGORY[category] || FALLBACK;
  return BASE + id + PARAMS;
}
