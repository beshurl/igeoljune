package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.ai.*;
import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.Recipient;
import com.skala.ikgeoljune.dto.preference.ExtractedPreferenceResponse;
import com.skala.ikgeoljune.dto.preference.KakaoAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * KAKAO-001 카카오톡 파일 임시 분석.
 *
 * <p>원문 파일은 DB 에 저장하지 않고 메모리에서만 사용한 뒤 즉시 버린다.
 * 사용자가 확인한 항목만 PREF-002 로 저장된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoAnalysisService {

    private final OwnershipValidator ownershipValidator;
    private final GiftAiClient giftAiClient;

    @Value("${app.kakao-analysis.max-file-size-bytes}")
    private long maxFileSizeBytes;

    @Value("${app.kakao-analysis.allowed-extensions}")
    private String allowedExtensions;

    public KakaoAnalysisResponse analyze(Long recipientId, Long userId, MultipartFile file) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);
        validate(file);

        String chatText = readText(file);
        try {
            List<AiExtractedPreference> extracted = giftAiClient.extractPreferences(
                    new AiKakaoAnalysisContext(toProfile(recipient), chatText));

            List<ExtractedPreferenceResponse> items = extracted.stream()
                    .map(e -> new ExtractedPreferenceResponse(e.preferenceType(), e.preferenceValue()))
                    .toList();
            return KakaoAnalysisResponse.of(items);
        } catch (AiException e) {
            log.warn("카카오톡 분석 실패 - recipientId={}", recipientId, e);
            throw new ApiException(ErrorCode.AI_RESULT_INVALID, e.getMessage());
        } finally {
            // 원문은 여기서 참조가 끊기고 GC 대상이 된다. 파일은 디스크에 남기지 않는다.
            chatText = null;
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "분석할 파일이 필요합니다.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new ApiException(ErrorCode.PAYLOAD_TOO_LARGE);
        }
        String extension = extensionOf(file.getOriginalFilename());
        List<String> allowed = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
        if (!allowed.contains(extension)) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "지원하지 않는 파일 형식입니다. (" + String.join(", ", allowed) + ")");
        }
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String readText(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.AI_RESULT_INVALID, "파일을 읽을 수 없습니다.");
        }
    }

    private AiRecipientProfile toProfile(Recipient recipient) {
        return new AiRecipientProfile(
                recipient.getName(),
                recipient.getRelationship(),
                recipient.getAgeGroup(),
                recipient.getGender(),
                recipient.getJob()
        );
    }
}
