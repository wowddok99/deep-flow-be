package com.deepflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Consolidated dashboard stats (dashboard + weeklyTrend + dayOfWeek + hourly + activity)")
public record StatsDashboardAllResponse(
        DashboardOverviewResponse dashboard,
        List<WeeklyTrendResponse> weeklyTrend,
        List<DayOfWeekStatsResponse> dayOfWeek,
        List<HourlyDistributionResponse> hourly,
        LogActivityResponse activity
) {}
