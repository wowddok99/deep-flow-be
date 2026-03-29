package com.deepflow.domain.session.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LogUpdatedEvent {
    private final Long sessionId;
    private final Long userId;
}
