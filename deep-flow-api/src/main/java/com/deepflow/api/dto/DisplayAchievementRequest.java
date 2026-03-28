package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Display achievement update request")
public record DisplayAchievementRequest(
    @Schema(description = "Achievement code to display", example = "D-03")
    @NotBlank
    String achievementCode
) {}
