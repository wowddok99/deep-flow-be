package com.deepflow.api.controller.stats;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.dto.DailyStatsResponse;
import com.deepflow.api.dto.StatsOverviewResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.stats.DailyFocusStatsService;
import com.deepflow.domain.stats.DailyFocusStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stats", description = "Focus statistics API")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final DailyFocusStatsService dailyFocusStatsService;

    @Operation(summary = "Get stats overview (today + this week)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats overview retrieved")
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weekly stats retrieved")
    })
    @GetMapping("/weekly")
    public ResponseEntity<CommonResponse<List<DailyStatsResponse>>> getWeeklyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<DailyFocusStats> stats = dailyFocusStatsService.getWeeklyStats(userDetails.getUserId());
        List<DailyStatsResponse> response = stats.stream()
                .map(s -> new DailyStatsResponse(s.getDate(), s.getTotalSessions(), s.getTotalDurationSeconds()))
                .toList();
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
