package com.deepflow.application.crew.dto;

import com.deepflow.domain.crew.CrewRole;
import com.deepflow.domain.crew.Visibility;

import java.time.LocalDateTime;
import java.util.List;

public record CrewDetailInfo(
        Long id,
        String name,
        String description,
        Visibility visibility,
        Integer maxMembers,
        long memberCount,
        int activeNowCount,
        CrewRole myRole,
        String inviteCode,
        LocalDateTime inviteCodeExpiresAt,
        LocalDateTime createdAt,
        List<CrewMemberInfo> members
) {}
