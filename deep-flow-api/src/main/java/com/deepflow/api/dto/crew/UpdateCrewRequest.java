package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewUpdateCommand;
import com.deepflow.domain.crew.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * PUT 시맨틱: name / visibility 는 필수.
 * description 은 null = 명시적 비움, maxMembers 는 null = 무제한.
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
