package com.deepflow.application.stats.dto;

import com.deepflow.domain.stats.DailyFocusStats;

import java.time.LocalDate;

public record DailyStatsInfo(
    LocalDate date,
    int totalSessions,
    long totalDurationSeconds
) {
    public static DailyStatsInfo from(DailyFocusStats stats) {
        return new DailyStatsInfo(
            stats.getDate(),
            stats.getTotalSessions(),
            stats.getTotalDurationSeconds()
        );
    }
}
