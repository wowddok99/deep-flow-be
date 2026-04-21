package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewDetailInfo;
import com.deepflow.domain.crew.CrewRole;
import com.deepflow.domain.crew.Visibility;

import java.time.LocalDateTime;
import java.util.List;

public record CrewDetailResponse(
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
        List<CrewMemberResponse> members
) {
    public static CrewDetailResponse from(CrewDetailInfo info) {
        return new CrewDetailResponse(
                info.id(),
                info.name(),
                info.description(),
                info.visibility(),
                info.maxMembers(),
                info.memberCount(),
                info.activeNowCount(),
                info.myRole(),
                info.inviteCode(),
                info.inviteCodeExpiresAt(),
                info.createdAt(),
                info.members().stream().map(CrewMemberResponse::from).toList()
        );
    }
}
