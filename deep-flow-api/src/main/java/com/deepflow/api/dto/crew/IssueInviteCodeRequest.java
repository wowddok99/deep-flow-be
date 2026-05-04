package com.deepflow.api.dto.crew;

import jakarta.validation.constraints.NotNull;

public record IssueInviteCodeRequest(
        @NotNull Integer ttlMinutes
) {}
