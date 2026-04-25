package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.SharedSessionInfo;

import java.time.LocalDateTime;
import java.util.List;

public record SharedSessionResponse(
        Long sessionId,
        Long crewId,
        LocalDateTime sharedAt,
        List<String> tags
) {
    public static SharedSessionResponse from(SharedSessionInfo info) {
        return new SharedSessionResponse(info.sessionId(), info.crewId(), info.sharedAt(), info.tags());
    }
}
