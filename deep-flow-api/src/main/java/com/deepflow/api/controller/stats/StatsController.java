package com.deepflow.api.controller.stats;

import com.deepflow.api.dto.*;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.stats.DailyFocusStatsService;
import com.deepflow.application.stats.StatsDashboardService;
import com.deepflow.application.stats.dto.DailyStatsInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stats", description = "Focus statistics API")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final DailyFocusStatsService dailyFocusStatsService;
    private final StatsDashboardService statsDashboardService;

    @Operation(summary = "Get stats overview (today + this week)")
    @GetMapping("/overview")
    public ResponseEntity<CommonResponse<StatsOverviewResponse>> getOverview(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        DailyFocusStatsService.StatsOverview overview = dailyFocusStatsService.getOverview(userDetails.getUserId());
        StatsOverviewResponse response = new StatsOverviewResponse(
                overview.todaySessions(),
                overview.todayDurationSeconds(),
                overview.weekSessions(),
                overview.weekDurationSeconds()
        );
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get daily stats for the last 7 days")
    @GetMapping("/weekly")
    public ResponseEntity<CommonResponse<List<DailyStatsResponse>>> getWeeklyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<DailyStatsInfo> stats = dailyFocusStatsService.getWeeklyStats(userDetails.getUserId());
        List<DailyStatsResponse> response = stats.stream()
                .map(DailyStatsResponse::from)
                .toList();
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get dashboard overview (cards)")
    @GetMapping("/dashboard")
    public ResponseEntity<CommonResponse<DashboardOverviewResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(
                DashboardOverviewResponse.from(statsDashboardService.getDashboardOverview(userDetails.getUserId()))
        ));
    }

    @Operation(summary = "Get weekly trend")
    @GetMapping("/weekly-trend")
    public ResponseEntity<CommonResponse<List<WeeklyTrendResponse>>> getWeeklyTrend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "4") int weeks
    ) {
        List<WeeklyTrendResponse> response = statsDashboardService.getWeeklyTrend(userDetails.getUserId(), weeks)
                .stream().map(WeeklyTrendResponse::from).toList();
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get day of week distribution")
    @GetMapping("/day-of-week")
    public ResponseEntity<CommonResponse<List<DayOfWeekStatsResponse>>> getDayOfWeek(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<DayOfWeekStatsResponse> response = statsDashboardService.getDayOfWeekDistribution(userDetails.getUserId())
                .stream().map(DayOfWeekStatsResponse::from).toList();
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get hourly session distribution")
    @GetMapping("/hourly")
    public ResponseEntity<CommonResponse<List<HourlyDistributionResponse>>> getHourly(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<HourlyDistributionResponse> response = statsDashboardService.getHourlyDistribution(userDetails.getUserId())
                .stream().map(HourlyDistributionResponse::from).toList();
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get monthly calendar heatmap data")
    @GetMapping("/calendar")
    public ResponseEntity<CommonResponse<List<DailyStatsResponse>>> getCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<DailyStatsResponse> response = statsDashboardService.getCalendarData(userDetails.getUserId(), year, month)
                .stream().map(DailyStatsResponse::from).toList();
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Operation(summary = "Get log activity summary")
    @GetMapping("/activity")
    public ResponseEntity<CommonResponse<LogActivityResponse>> getActivity(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(
                LogActivityResponse.from(statsDashboardService.getLogActivity(userDetails.getUserId()))
        ));
    }

    @Operation(summary = "Get all dashboard stats (consolidated — excludes calendar)")
    @GetMapping("/all")
    public ResponseEntity<CommonResponse<StatsDashboardAllResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(CommonResponse.ok(
                new StatsDashboardAllResponse(
                        DashboardOverviewResponse.from(statsDashboardService.getDashboardOverview(userId)),
                        statsDashboardService.getWeeklyTrend(userId, 4).stream().map(WeeklyTrendResponse::from).toList(),
                        statsDashboardService.getDayOfWeekDistribution(userId).stream().map(DayOfWeekStatsResponse::from).toList(),
                        statsDashboardService.getHourlyDistribution(userId).stream().map(HourlyDistributionResponse::from).toList(),
                        LogActivityResponse.from(statsDashboardService.getLogActivity(userId))
                )
        ));
    }
}
