package com.deepflow.api.dto;

import com.deepflow.application.stats.dto.DayOfWeekStatsInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Day of week distribution stats")
public record DayOfWeekStatsResponse(
        String dayOfWeek,
        long totalSessions,
        long totalDurationSeconds
) {
    public static DayOfWeekStatsResponse from(DayOfWeekStatsInfo info) {
        return new DayOfWeekStatsResponse(info.dayOfWeek(), info.totalSessions(), info.totalDurationSeconds());
    }
}
