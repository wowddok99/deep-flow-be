package com.deepflow.api.dto;

import com.deepflow.application.stats.dto.DailyStatsInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Daily focus stats")
public record DailyStatsResponse(
        @Schema(description = "Date", example = "2025-01-15")
        LocalDate date,
        @Schema(description = "Completed sessions count", example = "3")
        int totalSessions,
        @Schema(description = "Total focus duration in seconds", example = "5400")
        long totalDurationSeconds
) {
    public static DailyStatsResponse from(DailyStatsInfo info) {
        return new DailyStatsResponse(info.date(), info.totalSessions(), info.totalDurationSeconds());
    }
}
