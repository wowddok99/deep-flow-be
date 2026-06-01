package com.deepflow.domain.session.reaction;

import java.util.Arrays;

public enum ReactionEmoji {
    THUMBS_UP("👍"),
    FIRE("🔥"),
    COFFEE("☕"),
    LIGHT_BULB("💡"),
    BULLSEYE("🎯");

    private final String unicode;

    ReactionEmoji(String unicode) {
        this.unicode = unicode;
    }

    public String unicode() {
        return unicode;
    }

    public static ReactionEmoji fromUnicode(String s) {
        if (s == null) return null;
        return Arrays.stream(values())
                .filter(e -> e.unicode.equals(s))
                .findFirst()
                .orElse(null);
    }
}
