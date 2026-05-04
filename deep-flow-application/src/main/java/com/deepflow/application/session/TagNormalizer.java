package com.deepflow.application.session;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 태그 정규화.
 *
 * 규칙:
 *  - lowercase
 *  - 공백 → '-' 치환
 *  - 허용 문자: a-z, 0-9, 한글, '-', '+', '#', '.'
 *    (개발자 태그 c++, c#, .net, node.js 보존)
 *  - 길이 30자 이내
 *  - 정규화 후 알파벳/숫자/한글이 하나도 없으면 빈 문자열로 반환 (호출자가 무시)
 */
@Component
public class TagNormalizer {

    private static final int MAX_LENGTH = 30;
    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-z0-9가-힣\\-+#.]");
    private static final Pattern HAS_ALNUM_OR_HANGUL = Pattern.compile(".*[a-z0-9가-힣].*");

    public String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return "";

        // '#' 은 의도적 prefix 표기인 경우만 제거. 중간 '#' (c#) 은 보존.
        if (s.startsWith("#")) s = s.substring(1);

        s = s.replaceAll("\\s+", "-");
        s = INVALID_CHARS.matcher(s).replaceAll("");

        if (s.isEmpty()) return "";
        // '+++' 처럼 의미 없는 기호만 남으면 폐기
        if (!HAS_ALNUM_OR_HANGUL.matcher(s).matches()) return "";

        if (s.length() > MAX_LENGTH) s = s.substring(0, MAX_LENGTH);
        return s;
    }
}
