package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewCreateCommand;
import com.deepflow.domain.crew.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCrewRequest(
        @NotBlank @Size(max = 30) String name,
        @Size(max = 200) String description,
        Visibility visibility,
        Integer maxMembers
) {
    public CrewCreateCommand toCommand() {
        return new CrewCreateCommand(name, description,
                visibility == null ? Visibility.PRIVATE : visibility,
                maxMembers);
    }
}
