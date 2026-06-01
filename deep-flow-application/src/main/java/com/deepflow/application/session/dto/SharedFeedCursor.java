package com.deepflow.application.session.dto;

import com.deepflow.application.exception.session.InvalidCursorException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 크루 피드 정렬 키 기반 페이지네이션 커서
 *
 * 정렬 키를 불투명 토큰으로 감춰 향후 직렬화 형식 변경이 API 시그니처에 노출되지 않도록 유지
 */
public record SharedFeedCursor(LocalDateTime sharedAt, Long id) {

    private static final String SEPARATOR = "|";

    public String encode() {
        String raw = sharedAt.toString() + SEPARATOR + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static SharedFeedCursor decode(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            int sep = raw.lastIndexOf(SEPARATOR);
            if (sep <= 0 || sep == raw.length() - 1) {
                throw new InvalidCursorException();
            }
            LocalDateTime sharedAt = LocalDateTime.parse(raw.substring(0, sep));
            Long id = Long.parseLong(raw.substring(sep + 1));
            return new SharedFeedCursor(sharedAt, id);
        } catch (InvalidCursorException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCursorException();
        }
    }
}
