package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewUpdateCommand;
import com.deepflow.domain.crew.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 전체 수정 요청에서 description null 은 명시적 비움, maxMembers null 은 무제한으로 처리
 */
public record UpdateCrewRequest(
        @NotBlank @Size(max = 30) String name,
        @Size(max = 200) String description,
        Visibility visibility,
        Integer maxMembers
) {
    public CrewUpdateCommand toCommand() {
        return new CrewUpdateCommand(name, description, visibility, maxMembers);
    }
}
