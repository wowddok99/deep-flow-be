package com.deepflow.domain.session.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionUnsharedEvent {
    private final Long sessionId;
    private final Long crewId;
    private final Long actorUserId;
}
