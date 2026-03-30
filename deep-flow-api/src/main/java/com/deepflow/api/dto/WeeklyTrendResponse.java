package com.deepflow.api.dto;

import com.deepflow.application.stats.dto.WeeklyTrendInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Weekly trend stats")
public record WeeklyTrendResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        int totalSessions,
        long totalDurationSeconds
) {
    public static WeeklyTrendResponse from(WeeklyTrendInfo info) {
        return new WeeklyTrendResponse(info.weekStart(), info.weekEnd(), info.totalSessions(), info.totalDurationSeconds());
    }
}
