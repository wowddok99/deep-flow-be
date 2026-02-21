package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Login request")
public record LoginRequest(
        @Schema(description = "Username", example = "testuser")
        @NotBlank String username,
        @Schema(description = "Password", example = "password123")
        @NotBlank String password
) {
}
