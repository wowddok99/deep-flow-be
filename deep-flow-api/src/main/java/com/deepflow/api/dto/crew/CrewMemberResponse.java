package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewMemberInfo;
import com.deepflow.domain.crew.CrewRole;

import java.time.LocalDateTime;

public record CrewMemberResponse(
        Long userId,
        String name,
        CrewRole role,
        LocalDateTime joinedAt,
        boolean isActiveNow
) {
    public static CrewMemberResponse from(CrewMemberInfo info) {
        return new CrewMemberResponse(
                info.userId(),
                info.name(),
                info.role(),
                info.joinedAt(),
                info.isActiveNow()
        );
    }
}
