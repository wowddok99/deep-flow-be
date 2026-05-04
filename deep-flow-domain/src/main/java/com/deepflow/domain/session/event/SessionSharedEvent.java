package com.deepflow.domain.session.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SessionSharedEvent {
    private final Long sessionId;
    private final Long crewId;
    private final Long actorUserId;
    private final List<String> tags;
}
