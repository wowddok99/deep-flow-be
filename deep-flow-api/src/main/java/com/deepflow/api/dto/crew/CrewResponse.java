package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewSummaryInfo;
import com.deepflow.domain.crew.CrewRole;
import com.deepflow.domain.crew.Visibility;

import java.time.LocalDateTime;

public record CrewResponse(
        Long id,
        String name,
        String description,
        Visibility visibility,
        Integer maxMembers,
        long memberCount,
        int activeNowCount,
        CrewRole role,
        LocalDateTime createdAt
) {
    public static CrewResponse from(CrewSummaryInfo info) {
        return new CrewResponse(
                info.id(),
                info.name(),
                info.description(),
                info.visibility(),
                info.maxMembers(),
                info.memberCount(),
                info.activeNowCount(),
                info.role(),
                info.createdAt()
        );
    }
}
