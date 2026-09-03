# 이걸주네? — Backend

Spring Boot 3 (Layered Architecture) + PostgreSQL + Spring AI (ChatClient)

## 실행 (로컬 PostgreSQL 필요 — `database/` 폴더의 docker-compose 참고)

```bash
export OPENAI_API_KEY=sk-...   # Spring AI ChatClient용 (없으면 추천 요청 시 FAILED 상태 반환)
mvn spring-boot:run
# (Maven Wrapper가 필요하면 최초 1회: mvn -N wrapper:wrapper 실행 후 ./mvnw 사용)
```

기본 포트: `8080`, API prefix: `/api`, Swagger UI: `/swagger-ui.html`

> 이 컨테이너는 외부 네트워크가 제한되어 있어 `mvn`으로 의존성을 직접 내려받아 빌드 검증을 하지 못했습니다.
> 팀원 로컬 환경(정상 인터넷)에서 `./mvnw clean install`로 첫 빌드를 검증해 주세요.

## 레이어드 아키텍처 폴더 구조

```
src/main/java/com/skala/ikgeoljune/
  controller/   REST API 엔드포인트 (요청/응답만 담당)
  service/      비즈니스 로직 — AiGiftAdvisor가 AI 확장 지점(UC8)
  repository/   Spring Data JPA 리포지토리
  domain/       JPA 엔티티 (테이블 = snake_case)
  dto/
    request/    API 요청 바디
    response/   API 응답 바디
  config/       WebConfig(CORS), AiConfig(ChatClient 빈)
  exception/    공통 예외 처리
```

## 대표 흐름 (UC7 → UC8 → UC9) 코드 위치

1. `GiftConditionController` → `GiftRecommendationService.createCondition` (UC7)
2. `RecommendationController.requestRecommendation` → `GiftRecommendationService.requestRecommendation`
   → **`AiGiftAdvisor.recommend`** (UC8, AI 확장 지점 — 시스템/유저 프롬프트와 JSON 스키마가 이 클래스에 있음)
3. `RecommendationController.get` (UC9)
4. `RecommendationController.feedback` / `reRecommend` (UC10·UC11·UC12)
5. `RecommendationController.confirm` → `GiftHistoryService.confirm` (UC13)

`Recommendation.status`는 `PENDING/COMPLETED/FAILED`로 AI 비동기 파이프라인을 표현합니다 (제출가이드 §2 권장 필드).

## API ↔ 화면(SCR) ↔ Use-Case 매핑

| Controller | SCR ID | UC |
|---|---|---|
| AuthController | SCR-AUTH-001 | UC1 |
| RecipientController | SCR-RECIPIENT-001 | UC2 |
| GiftConditionController | SCR-GIFT-001 | UC3·UC7 |
| RecommendationController | SCR-AI-001 / SCR-AI-002 | UC8~UC12 |
| GiftHistoryController | SCR-HISTORY-001 | UC13·UC14 |

## 다음 할 일 (팀원 배분용)

- [ ] Google OAuth ID Token 실제 검증 (`AuthService`)
- [ ] JWT 발급/검증 (`Spring Security` 추가 권장)
- [ ] `RecipientController`가 로그인 사용자 기준으로 동작하도록 인증 연동
- [ ] `GiftHistoryController` 응답을 엔티티 대신 DTO로 변환
- [ ] Calendar/카카오톡 컨트롤러·서비스 (선택 흐름) 구현
- [ ] Flyway로 `ddl-auto: update` 대체 (운영 전환 시)
