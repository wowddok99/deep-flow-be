package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewUpdateCommand;
import com.deepflow.domain.crew.Visibility;
import jakarta.validation.constraints.Size;

public record UpdateCrewRequest(
        @Size(max = 30) String name,
        @Size(max = 200) String description,
        Visibility visibility,
        Integer maxMembers
) {
    public CrewUpdateCommand toCommand() {
        return new CrewUpdateCommand(name, description, visibility, maxMembers);
    }
}
