package com.deepflow.core.domain.session.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionStoppedEvent {
    private final Long sessionId;
    private final Long userId;
    private final long durationSeconds;
}
