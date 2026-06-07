package com.deepflow.application.session.reaction.dto;

public record ReactionToggleResult(
        String emoji,
        boolean added,
        int totalCount,
        boolean userReacted
) {}
