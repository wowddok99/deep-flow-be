package com.deepflow.application.stats.dto;

public record DayOfWeekStatsInfo(
    String dayOfWeek,
    long totalSessions,
    long totalDurationSeconds
) {}
