package com.deepflow.application.crew.dto;

public record CrewPresencePayload(
        Long crewId,
        Long userId,
        String userName,
        boolean isActive,
        long activeNowCount
) {}
