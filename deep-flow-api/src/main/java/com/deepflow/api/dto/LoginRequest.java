package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Login request")
public record LoginRequest(
        @Schema(description = "Username (max 20 chars)", example = "testuser")
        @NotBlank @Size(max = 20, message = "Username must not exceed 20 characters") String username,
        @Schema(description = "Password (max 72 chars)", example = "password123")
        @NotBlank @Size(max = 72, message = "Password must not exceed 72 characters") String password
) {
}
