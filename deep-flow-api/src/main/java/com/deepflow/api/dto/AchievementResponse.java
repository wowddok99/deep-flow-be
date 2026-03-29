package com.deepflow.api.dto;

import com.deepflow.application.achievement.dto.AchievementInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Achievement response")
public record AchievementResponse(
    @Schema(description = "Achievement code", example = "D-01")
    String code,
    @Schema(description = "Achievement name", example = "잔잔한 호수")
    String name,
    @Schema(description = "Achievement description")
    String description,
    @Schema(description = "Category")
    String category,
    @Schema(description = "Grade (1-5)")
    int grade,
    @Schema(description = "Hidden achievement")
    boolean hidden,
    @Schema(description = "Whether user achieved this")
    boolean achieved
) {
    public static AchievementResponse from(AchievementInfo info) {
        return new AchievementResponse(
            info.code(), info.name(), info.description(),
            info.category().name(), info.grade(), info.hidden(), info.achieved()
        );
    }
}
