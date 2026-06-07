package com.deepflow.api.dto.session;

import com.deepflow.application.session.reaction.dto.ReactionToggleResult;

public record ReactionToggleResponse(
        String emoji,
        boolean added,
        int totalCount,
        boolean userReacted
) {
    public static ReactionToggleResponse from(ReactionToggleResult r) {
        return new ReactionToggleResponse(r.emoji(), r.added(), r.totalCount(), r.userReacted());
    }
}
