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
        @Schema(description = "Password (8-72 chars)", example = "password123")
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") String password,
        @Schema(description = "Display name (2-20 chars)", example = "Test User")
        @NotBlank @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters") String name
) {
}
