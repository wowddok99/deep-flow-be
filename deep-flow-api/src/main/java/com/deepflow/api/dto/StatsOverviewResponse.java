package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Focus stats overview")
public record StatsOverviewResponse(
        @Schema(description = "Today's completed sessions", example = "3")
        int todaySessions,
        @Schema(description = "Today's total focus duration in seconds", example = "5400")
        long todayDurationSeconds,
        @Schema(description = "This week's completed sessions (last 7 days)", example = "15")
        int weekSessions,
        @Schema(description = "This week's total focus duration in seconds", example = "36000")
        long weekDurationSeconds
) {
}
