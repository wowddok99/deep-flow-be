package com.deepflow.application.crew.dto;

import com.deepflow.domain.crew.CrewRole;

import java.time.LocalDateTime;

public record CrewMemberInfo(
        Long userId,
        String name,
        CrewRole role,
        LocalDateTime joinedAt,
        boolean isActiveNow
) {}
