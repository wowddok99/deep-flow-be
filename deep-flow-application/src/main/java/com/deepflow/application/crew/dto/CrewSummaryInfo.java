package com.deepflow.application.crew.dto;

import com.deepflow.domain.crew.Crew;
import com.deepflow.domain.crew.CrewRole;
import com.deepflow.domain.crew.Visibility;

import java.time.LocalDateTime;

public record CrewSummaryInfo(
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
    public static CrewSummaryInfo of(Crew crew, long memberCount, int activeNowCount, CrewRole role) {
        return new CrewSummaryInfo(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getVisibility(),
                crew.getMaxMembers(),
                memberCount,
                activeNowCount,
                role,
                crew.getCreatedAt()
        );
    }
}
