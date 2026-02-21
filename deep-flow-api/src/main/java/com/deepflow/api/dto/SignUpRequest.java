package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Sign up request")
public record SignUpRequest(
        @Schema(description = "Username (6-20 chars)", example = "testuser")
        @NotBlank @Size(min = 6, max = 20, message = "Username must be between 6 and 20 characters") String username,
        @Schema(description = "Password", example = "password123")
        @NotBlank String password,
        @Schema(description = "Display name", example = "Test User")
        @NotBlank String name
) {
}
