package com.deepflow.application.crew.dto;

import com.deepflow.domain.crew.Visibility;

public record CrewUpdateCommand(
        String name,
        String description,
        Visibility visibility,
        Integer maxMembers
) {}
