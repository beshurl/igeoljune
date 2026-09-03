package com.skala.ikgeoljune.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API 명세서 대표 흐름 통합 테스트.
 * AUTH-001 → AUTH-002 → RECIPIENT-001 → KAKAO-001 → PREF-002 → PREVGIFT-001
 * → CONDITION-001 → RECOMMEND-001 → RECOMMEND-002 → FEEDBACK-001 → RECOMMEND-004
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GiftRecommendationFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입부터 재추천까지 명세서 흐름이 동작한다")
    void fullFlow() throws Exception {
        // AUTH-001 회원가입
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"flow@example.com","password":"password123!","name":"홍길동"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("flow@example.com"));

        // 이메일 중복은 409
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"flow@example.com","password":"password123!","name":"홍길동"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        // AUTH-002 로그인
        JsonNode login = readJson(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"flow@example.com","password":"password123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn());
        String token = "Bearer " + login.get("accessToken").asText();

        // 인증 없이 접근하면 401
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // USER-001
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"));

        // RECIPIENT-001
        JsonNode recipient = readJson(mockMvc.perform(post("/api/v1/recipients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"철수","relationship":"FRIEND","ageGroup":"LATE_20S","gender":"MALE","job":"개발자"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        long recipientId = recipient.get("recipientId").asLong();

        // KAKAO-001 원문은 저장하지 않고 분석 결과만 반환
        MockMultipartFile file = new MockMultipartFile("file", "chat.txt", "text/plain",
                """
                        [철수] 요즘 홈카페에 빠져서 드립 커피만 마셔
                        [철수] 무선충전기 하나 사고 싶은데 계속 미루는 중
                        [철수] 향수는 별로야 취향 아니더라
                        """.getBytes(StandardCharsets.UTF_8));

        JsonNode analyzed = readJson(mockMvc.perform(multipart("/api/v1/recipients/{id}/kakao-analysis", recipientId)
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(analyzed.get("items")).isNotEmpty();
        // KAKAO-001 은 저장 전 결과라 items 만 내려간다 (totalCount 없음)
        assertThat(analyzed.has("totalCount")).isFalse();

        // PREF-002 확인한 항목만 저장
        mockMvc.perform(post("/api/v1/recipients/{id}/preferences/bulk", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceType":"KAKAO","items":[
                                  {"preferenceType":"INTEREST","preferenceValue":"홈카페"},
                                  {"preferenceType":"WISH_ITEM","preferenceValue":"무선 충전기"},
                                  {"preferenceType":"DISLIKED_CATEGORY","preferenceValue":"향수"}
                                ]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCount").value(3));

        // PREF-003
        mockMvc.perform(get("/api/v1/recipients/{id}/preferences", recipientId)
                        .header("Authorization", token)
                        .param("sourceType", "KAKAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(3));

        // PREVGIFT-001 과거 선물은 추천에서 제외된다
        mockMvc.perform(post("/api/v1/recipients/{id}/previous-gifts", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"giftName":"보온 텀블러","giftCategory":"LIVING","giftedAt":"2025-09-20","note":"지난 생일에 선물"}
                                """))
                .andExpect(status().isCreated());

        // CONDITION-001
        JsonNode condition = readJson(mockMvc.perform(post("/api/v1/recipients/{id}/gift-conditions", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"budgetMin":30000,"budgetMax":70000,"occasionType":"BIRTHDAY",
                                 "occasionDate":"2026-09-20","preferenceNote":"실용적인 상품을 추천해 주세요.",
                                 "avoidGiftNote":"전자기기는 제외해 주세요."}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        long conditionId = condition.get("conditionId").asLong();

        // 예산 역전은 400
        mockMvc.perform(post("/api/v1/recipients/{id}/gift-conditions", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"budgetMin":90000,"budgetMax":10000,"occasionType":"BIRTHDAY"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // RECOMMEND-001 — 201 Created 로 후보를 즉시 반환한다 (폴링 없음)
        JsonNode detail = readJson(mockMvc.perform(
                        post("/api/v1/gift-conditions/{id}/recommendations", conditionId)
                                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.candidates").isNotEmpty())
                .andReturn());
        long recommendationId = detail.get("recommendationId").asLong();
        assertThat(detail.get("failure").isNull()).isTrue();
        assertThat(detail.get("updatedAt").isNull()).isFalse();

        // 같은 내용을 RECOMMEND-002 로도 다시 조회할 수 있다
        JsonNode reloaded = readJson(mockMvc.perform(get("/api/v1/recommendations/{id}", recommendationId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(reloaded.get("candidates").size()).isEqualTo(detail.get("candidates").size());

        JsonNode firstCandidate = detail.get("candidates").get(0);
        assertThat(firstCandidate.get("recommendRank").asInt()).isEqualTo(1);
        assertThat(firstCandidate.get("createdAt").isNull()).isFalse();
        assertThat(firstCandidate.get("feedback").isNull()).isTrue();

        // 가격은 예산으로 보정하지 않는다 (min <= max 만 보장)
        for (JsonNode candidate : detail.get("candidates")) {
            assertThat(candidate.get("estimatedPriceMin").asInt())
                    .isLessThanOrEqualTo(candidate.get("estimatedPriceMax").asInt());
        }

        // 과거 선물과 avoidGiftNote(전자기기)는 후보에서 빠진다
        for (JsonNode candidate : detail.get("candidates")) {
            assertThat(candidate.get("giftName").asText()).isNotEqualTo("보온 텀블러");
            assertThat(candidate.get("giftCategory").asText()).isNotEqualTo("TECH");
        }

        // CONDITION-004 추천 결과가 있으면 409
        mockMvc.perform(delete("/api/v1/gift-conditions/{id}", conditionId)
                        .header("Authorization", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        long candidateId = firstCandidate.get("candidateId").asLong();

        // FEEDBACK-002 등록 전에는 404
        mockMvc.perform(get("/api/v1/recommendation-candidates/{id}/feedback", candidateId)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // FEEDBACK-001 LIKE 등록 후 DISLIKE 로 변경 (upsert)
        JsonNode liked = readJson(mockMvc.perform(put("/api/v1/recommendation-candidates/{id}/feedback", candidateId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"feedbackType":"LIKE","dislikeReason":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackType").value("LIKE"))
                .andReturn());

        JsonNode disliked = readJson(mockMvc.perform(put("/api/v1/recommendation-candidates/{id}/feedback", candidateId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"feedbackType":"DISLIKE","dislikeReason":"ALREADY_OWNED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackType").value("DISLIKE"))
                .andExpect(jsonPath("$.dislikeReason").value("ALREADY_OWNED"))
                .andReturn());

        // 후보당 피드백은 1건만 유지된다
        assertThat(disliked.get("feedbackId").asLong()).isEqualTo(liked.get("feedbackId").asLong());

        // RECOMMEND-004 재추천 — previousRecommendationId 로 이전 추천을 연결
        JsonNode reDetail = readJson(mockMvc.perform(
                        post("/api/v1/recommendations/{id}/re-recommend", recommendationId)
                                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.previousRecommendationId").value(recommendationId))
                .andExpect(jsonPath("$.candidates").isNotEmpty())
                .andReturn());
        long reRecommendationId = reDetail.get("recommendationId").asLong();
        String dislikedGiftName = firstCandidate.get("giftName").asText();
        for (JsonNode candidate : reDetail.get("candidates")) {
            assertThat(candidate.get("giftName").asText()).isNotEqualTo(dislikedGiftName);
        }

        // RECOMMEND-003 생성일 역순
        mockMvc.perform(get("/api/v1/gift-conditions/{id}/recommendations", conditionId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.items[0].recommendationId").value(reRecommendationId));

        // FEEDBACK-003
        mockMvc.perform(delete("/api/v1/recommendation-candidates/{id}/feedback", candidateId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        // RECIPIENT-005 하위 데이터까지 삭제
        mockMvc.perform(delete("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // USER-003
        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("다른 사용자의 추천 대상에는 접근할 수 없다")
    void ownershipIsEnforced() throws Exception {
        String ownerToken = signupAndLogin("owner@example.com", "주인");
        String otherToken = signupAndLogin("other@example.com", "남");

        JsonNode recipient = readJson(mockMvc.perform(post("/api/v1/recipients")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"영희","relationship":"FAMILY","ageGroup":"EARLY_30S","gender":"FEMALE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        long recipientId = recipient.get("recipientId").asLong();

        mockMvc.perform(get("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESOURCE_FORBIDDEN"));
    }

    @Test
    @DisplayName("PATCH 로 빈 문자열을 보내면 생성 요청과 동일하게 거부한다")
    void patchRejectsBlankRequiredFields() throws Exception {
        String token = signupAndLogin("blank@example.com", "빈값");
        long recipientId = createRecipient(token);

        mockMvc.perform(patch("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        // 필드를 아예 보내지 않는 것은 그대로 허용된다
        mockMvc.perform(patch("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"job":"백엔드 개발자"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("철수"));
    }

    @Test
    @DisplayName("PATCH 응답의 updatedAt 이 저장된 값과 일치한다")
    void patchResponseCarriesFreshUpdatedAt() throws Exception {
        String token = signupAndLogin("touch@example.com", "갱신");
        long recipientId = createRecipient(token);

        JsonNode before = readJson(mockMvc.perform(get("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token))
                .andReturn());

        JsonNode patched = readJson(mockMvc.perform(patch("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"job":"백엔드 개발자"}
                                """))
                .andExpect(status().isOk())
                .andReturn());

        // 응답이 수정 전 시각 그대로면 안 된다
        assertThat(patched.get("updatedAt").asText())
                .isNotEqualTo(before.get("updatedAt").asText());

        // 응답 값과 실제 저장 값이 같아야 한다
        JsonNode reloaded = readJson(mockMvc.perform(get("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token))
                .andReturn());
        assertThat(reloaded.get("updatedAt").asText())
                .isEqualTo(patched.get("updatedAt").asText());
    }

    @Test
    @DisplayName("경로는 있으나 메서드가 다르면 404 가 아니라 405 를 반환한다")
    void wrongMethodReturns405() throws Exception {
        String token = signupAndLogin("method@example.com", "메서드");

        mockMvc.perform(put("/api/v1/recipients").header("Authorization", token))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("API.yml 요청 스키마 제약을 지킨다 (minProperties, additionalProperties, page/size 범위)")
    void enforcesRequestContract() throws Exception {
        String token = signupAndLogin("contract@example.com", "계약");
        long recipientId = createRecipient(token);

        // minProperties: 1 — 빈 PATCH 본문 거부
        mockMvc.perform(patch("/api/v1/recipients/{id}", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // additionalProperties: false — 모르는 필드 거부
        mockMvc.perform(post("/api/v1/recipients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"영희","relationship":"FAMILY","ageGroup":"EARLY_30S","gender":"FEMALE","unknown":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // page/size 범위
        mockMvc.perform(get("/api/v1/recipients").header("Authorization", token).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("size"));

        mockMvc.perform(get("/api/v1/recipients").header("Authorization", token)
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @DisplayName("추천 생성 응답은 Location 헤더로 조회 URI 를 알려준다")
    void createdResponseCarriesLocationHeader() throws Exception {
        String token = signupAndLogin("location@example.com", "위치");
        long recipientId = createRecipient(token);

        JsonNode condition = readJson(mockMvc.perform(post("/api/v1/recipients/{id}/gift-conditions", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"budgetMin":30000,"budgetMax":70000,"occasionType":"BIRTHDAY"}
                                """))
                .andReturn());
        long conditionId = condition.get("conditionId").asLong();

        JsonNode created = readJson(mockMvc.perform(
                        post("/api/v1/gift-conditions/{id}/recommendations", conditionId)
                                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn());

        assertThat(created.get("updatedAt").isNull()).isFalse();
        assertThat(created.get("candidates")).isNotEmpty();
    }

    @Test
    @DisplayName("추천 후보 하나를 최종 선물로 선택하면 selectedAt 이 기록된다")
    void selectsFinalGift() throws Exception {
        String token = signupAndLogin("select@example.com", "선택");
        long recipientId = createRecipient(token);

        JsonNode condition = readJson(mockMvc.perform(post("/api/v1/recipients/{id}/gift-conditions", recipientId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"budgetMin":30000,"budgetMax":70000,"occasionType":"BIRTHDAY"}
                                """))
                .andReturn());

        JsonNode detail = readJson(mockMvc.perform(
                        post("/api/v1/gift-conditions/{id}/recommendations", condition.get("conditionId").asLong())
                                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn());

        long first = detail.get("candidates").get(0).get("candidateId").asLong();
        long second = detail.get("candidates").get(1).get("candidateId").asLong();

        // 선택 전에는 selectedAt 이 비어 있다
        assertThat(detail.get("candidates").get(0).get("selectedAt").isNull()).isTrue();

        mockMvc.perform(put("/api/v1/recommendation-candidates/{id}/selection", first)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId").value(first))
                .andExpect(jsonPath("$.selectedAt").exists());

        // 같은 추천 안에서 다른 후보를 선택하면 기존 선택은 해제된다
        mockMvc.perform(put("/api/v1/recommendation-candidates/{id}/selection", second)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedAt").exists());

        mockMvc.perform(get("/api/v1/recommendation-candidates/{id}/selection", first)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedAt").doesNotExist());

        // 추천 조회 응답에도 선택 상태가 함께 내려간다
        JsonNode reloaded = readJson(mockMvc.perform(
                        get("/api/v1/recommendations/{id}", detail.get("recommendationId").asLong())
                                .header("Authorization", token))
                .andReturn());
        long selectedCount = 0;
        for (JsonNode candidate : reloaded.get("candidates")) {
            if (!candidate.get("selectedAt").isNull()) {
                selectedCount++;
                assertThat(candidate.get("candidateId").asLong()).isEqualTo(second);
            }
        }
        assertThat(selectedCount).isEqualTo(1);

        // 선택 취소
        mockMvc.perform(delete("/api/v1/recommendation-candidates/{id}/selection", second)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/recommendation-candidates/{id}/selection", second)
                        .header("Authorization", token))
                .andExpect(jsonPath("$.selectedAt").doesNotExist());
    }

    private long createRecipient(String token) throws Exception {
        JsonNode recipient = readJson(mockMvc.perform(post("/api/v1/recipients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"철수","relationship":"FRIEND","ageGroup":"LATE_20S","gender":"MALE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        return recipient.get("recipientId").asLong();
    }

    private String signupAndLogin(String email, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123!","name":"%s"}
                                """.formatted(email, name)))
                .andExpect(status().isCreated());

        JsonNode login = readJson(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn());
        return "Bearer " + login.get("accessToken").asText();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
