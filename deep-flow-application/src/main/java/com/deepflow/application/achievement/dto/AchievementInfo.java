package com.deepflow.application.achievement.dto;

import com.deepflow.domain.achievement.Achievement;
import com.deepflow.domain.achievement.AchievementCategory;

public record AchievementInfo(
    String code,
    String name,
    String description,
    AchievementCategory category,
    int grade,
    boolean hidden,
    boolean achieved
) {
    public static AchievementInfo from(Achievement a, boolean achieved) {
        return new AchievementInfo(
            a.getCode(),
            a.isHidden() && !achieved ? "???" : a.getName(),
            a.isHidden() && !achieved ? "???" : a.getDescription(),
            a.getCategory(),
            a.getGrade(),
            a.isHidden(),
            achieved
        );
    }
}
