package com.deepflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignUpRequest(
        @NotBlank @Size(min = 6, max = 20, message = "Username must be between 6 and 20 characters") String username,
        @NotBlank String password,
        @NotBlank String name
) {
}
