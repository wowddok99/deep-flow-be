package com.deepflow.application.crew.dto;

import com.deepflow.domain.crew.Visibility;

public record CrewCreateCommand(
        String name,
        String description,
        Visibility visibility,
        Integer maxMembers
) {}
