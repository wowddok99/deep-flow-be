package com.deepflow.domain.session.event;

import com.deepflow.domain.session.reaction.ReactionEmoji;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionReactionRemovedEvent {
    private final Long sessionId;
    private final Long actorUserId;
    private final ReactionEmoji emoji;
}
