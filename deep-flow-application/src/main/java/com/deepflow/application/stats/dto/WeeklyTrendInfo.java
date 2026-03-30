package com.deepflow.application.stats.dto;

import java.time.LocalDate;

public record WeeklyTrendInfo(
    LocalDate weekStart,
    LocalDate weekEnd,
    int totalSessions,
    long totalDurationSeconds
) {}
