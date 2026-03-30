package com.deepflow.api.dto;

import com.deepflow.application.stats.dto.DashboardOverviewInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard overview stats")
public record DashboardOverviewResponse(
        long totalSessions,
        long totalDurationSeconds,
        long avgSessionDurationSeconds,
        int currentStreak,
        int longestStreak,
        long achievementCount,
        long totalAchievements,
        long thisWeekSessions,
        long thisWeekDurationSeconds,
        long lastWeekSessions,
        long lastWeekDurationSeconds
) {
    public static DashboardOverviewResponse from(DashboardOverviewInfo info) {
        return new DashboardOverviewResponse(
                info.totalSessions(), info.totalDurationSeconds(), info.avgSessionDurationSeconds(),
                info.currentStreak(), info.longestStreak(),
                info.achievementCount(), info.totalAchievements(),
                info.thisWeekSessions(), info.thisWeekDurationSeconds(),
                info.lastWeekSessions(), info.lastWeekDurationSeconds()
        );
    }
}
