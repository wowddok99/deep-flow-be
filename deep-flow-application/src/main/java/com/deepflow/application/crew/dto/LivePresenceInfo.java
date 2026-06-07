package com.deepflow.application.crew.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LivePresenceInfo(List<ActiveMember> activeMembers) {

    public record ActiveMember(Long userId, String name, LocalDateTime sessionStartedAt) {}
}
