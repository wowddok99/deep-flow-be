package com.deepflow.application.stats.dto;

public record DashboardOverviewInfo(
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
) {}
