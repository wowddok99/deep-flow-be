package com.deepflow.api.dto.crew;

import jakarta.validation.constraints.NotBlank;

public record JoinByCodeRequest(
        @NotBlank String code
) {}
