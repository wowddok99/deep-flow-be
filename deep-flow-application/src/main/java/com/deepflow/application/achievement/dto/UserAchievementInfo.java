package com.deepflow.application.achievement.dto;

import com.deepflow.domain.achievement.Achievement;
import com.deepflow.domain.achievement.AchievementCategory;
import com.deepflow.domain.achievement.UserAchievement;

import java.time.LocalDateTime;

public record UserAchievementInfo(
    String code,
    String name,
    String description,
    AchievementCategory category,
    int grade,
    boolean hidden,
    LocalDateTime achievedAt
) {
    public static UserAchievementInfo from(UserAchievement ua) {
        Achievement a = ua.getAchievement();
        return new UserAchievementInfo(
            a.getCode(), a.getName(), a.getDescription(),
            a.getCategory(), a.getGrade(), a.isHidden(),
            ua.getAchievedAt()
        );
    }
}
