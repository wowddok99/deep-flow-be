package com.deepflow.domain.session.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SessionCommentCreatedEvent {
    private final Long commentId;
    private final Long sessionId;
    private final Long actorUserId;
    private final List<Long> mentionedUserIds;
    private final String contentPreview;
}
