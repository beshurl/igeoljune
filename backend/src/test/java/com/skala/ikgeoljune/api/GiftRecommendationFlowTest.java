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
                .andExpect(jsonPath("$.code").value("EMAIL_DUPLICATED"));

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
        assertThat(analyzed.get("totalCount").asInt()).isGreaterThan(0);

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
                .andExpect(jsonPath("$.code").value("INVALID_BUDGET_RANGE"));

        // RECOMMEND-001 202 Accepted, PROCESSING
        JsonNode requested = readJson(mockMvc.perform(
                        post("/api/v1/gift-conditions/{id}/recommendations", conditionId)
                                .header("Authorization", token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.previousRecommendationId").doesNotExist())
                .andReturn());
        long recommendationId = requested.get("recommendationId").asLong();

        // RECOMMEND-002 완료까지 폴링
        JsonNode detail = awaitSuccess(token, recommendationId);
        assertThat(detail.get("candidates")).isNotEmpty();

        JsonNode firstCandidate = detail.get("candidates").get(0);
        assertThat(firstCandidate.get("recommendationRank").asInt()).isEqualTo(1);
        assertThat(firstCandidate.get("feedback").isNull()).isTrue();

        // 예산 범위 안으로 잘린 예상 가격
        assertThat(firstCandidate.get("estimatedPriceMin").asInt()).isGreaterThanOrEqualTo(30000);
        assertThat(firstCandidate.get("estimatedPriceMax").asInt()).isLessThanOrEqualTo(70000);

        // 과거 선물과 avoidGiftNote(전자기기)는 후보에서 빠진다
        for (JsonNode candidate : detail.get("candidates")) {
            assertThat(candidate.get("giftName").asText()).isNotEqualTo("보온 텀블러");
            assertThat(candidate.get("giftCategory").asText()).isNotEqualTo("TECH");
        }

        // CONDITION-004 추천 결과가 있으면 409
        mockMvc.perform(delete("/api/v1/gift-conditions/{id}", conditionId)
                        .header("Authorization", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("GIFT_CONDITION_HAS_RECOMMENDATIONS"));

        long candidateId = firstCandidate.get("candidateId").asLong();

        // FEEDBACK-002 등록 전에는 404
        mockMvc.perform(get("/api/v1/recommendation-candidates/{id}/feedback", candidateId)
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEEDBACK_NOT_FOUND"));

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
        JsonNode reRequested = readJson(mockMvc.perform(
                        post("/api/v1/recommendations/{id}/re-recommend", recommendationId)
                                .header("Authorization", token))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.previousRecommendationId").value(recommendationId))
                .andReturn());
        long reRecommendationId = reRequested.get("recommendationId").asLong();

        JsonNode reDetail = awaitSuccess(token, reRecommendationId);
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
                .andExpect(jsonPath("$.code").value("RECIPIENT_NOT_FOUND"));

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
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
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

    /** RECOMMEND-001 은 비동기이므로 SUCCESS 가 될 때까지 RECOMMEND-002 를 폴링한다. */
    private JsonNode awaitSuccess(String token, long recommendationId) throws Exception {
        for (int i = 0; i < 50; i++) {
            JsonNode detail = readJson(mockMvc.perform(get("/api/v1/recommendations/{id}", recommendationId)
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andReturn());

            String status = detail.get("status").asText();
            if ("SUCCESS".equals(status)) {
                return detail;
            }
            assertThat(status).isNotEqualTo("FAILED");
            Thread.sleep(100);
        }
        throw new AssertionError("추천이 제한 시간 안에 완료되지 않았습니다.");
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
