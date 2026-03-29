package com.deepflow.api.dto;

import com.deepflow.application.achievement.dto.UserAchievementInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "User achievement response")
public record UserAchievementResponse(
    @Schema(description = "Achievement code")
    String code,
    @Schema(description = "Achievement name")
    String name,
    @Schema(description = "Achievement description")
    String description,
    @Schema(description = "Category")
    String category,
    @Schema(description = "Grade")
    int grade,
    @Schema(description = "Achieved at")
    LocalDateTime achievedAt
) {
    public static UserAchievementResponse from(UserAchievementInfo info) {
        return new UserAchievementResponse(
            info.code(), info.name(), info.description(),
            info.category().name(), info.grade(), info.achievedAt()
        );
    }
}
