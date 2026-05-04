package com.deepflow.application.session.dto;

public record ReactionToggleResult(
        String emoji,
        boolean added,
        int totalCount,
        boolean userReacted
) {}
