package com.deepflow.application.session.dto;

import com.deepflow.application.exception.session.InvalidCursorException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 크루 피드 keyset 페이지네이션 커서.
 * 정렬 키 (sharedAt DESC, id DESC) 와 동일한 키를 보존해 직전 페이지의 마지막 위치를 가리킨다.
 *
 * 외부 표면은 불투명한 토큰으로만 노출 — 클라이언트는 받은 문자열을 그대로 다음 요청에 실어 보낸다.
 * 직렬화 형식은 내부 구현 디테일이며 향후 정렬 키 추가 시에도 API 시그니처를 흔들지 않기 위함.
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
