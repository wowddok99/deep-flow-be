package com.deepflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SignUpRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String name
) {
}
