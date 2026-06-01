package com.deepflow.application.stats.dto;

public record DashboardSummaryStats(
        long totalSessions,
        long totalDurationSeconds,
        long thisWeekSessions,
        long thisWeekDurationSeconds,
        long lastWeekSessions,
        long lastWeekDurationSeconds
) {
    public static DashboardSummaryStats empty() {
        return new DashboardSummaryStats(0L, 0L, 0L, 0L, 0L, 0L);
    }
}
