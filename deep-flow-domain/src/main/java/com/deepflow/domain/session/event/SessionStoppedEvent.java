package com.deepflow.domain.session.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SessionStoppedEvent {
    private final Long sessionId;
    private final Long userId;
    private final long durationSeconds;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
}
