package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.LivePresenceInfo;

import java.time.LocalDateTime;
import java.util.List;

public record LivePresenceResponse(List<ActiveMemberResponse> activeMembers) {

    public static LivePresenceResponse from(LivePresenceInfo info) {
        List<ActiveMemberResponse> members = info.activeMembers().stream()
                .map(m -> new ActiveMemberResponse(m.userId(), m.name(), m.sessionStartedAt()))
                .toList();
        return new LivePresenceResponse(members);
    }

    public record ActiveMemberResponse(Long userId, String name, LocalDateTime sessionStartedAt) {}
}
