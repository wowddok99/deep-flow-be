package com.deepflow.application.session;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 크루 공유 세션 태그 정규화
 *
 * c++, c#, .net, node.js 같은 개발자 태그를 보존하기 위해 +, #, . 문자는 허용
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

        // 사용자가 붙인 해시태그 기호만 제거하고 c# 같은 태그는 보존
        if (s.startsWith("#")) s = s.substring(1);

        s = s.replaceAll("\\s+", "-");
        s = INVALID_CHARS.matcher(s).replaceAll("");

        if (s.isEmpty()) return "";
        // +, #, . 만 남은 값은 검색 가능한 태그가 아니므로 폐기
        if (!HAS_ALNUM_OR_HANGUL.matcher(s).matches()) return "";

        if (s.length() > MAX_LENGTH) s = s.substring(0, MAX_LENGTH);
        return s;
    }
}
