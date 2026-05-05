package com.deepflow.application.stats.dto;

/**
 * 대시보드 개요 집계 통계 DTO.
 */
public record DashboardSummaryStats(
        long totalSessions,
        long totalDurationSeconds,
        long thisWeekSessions,
        long thisWeekDurationSeconds,
        long lastWeekSessions,
        long lastWeekDurationSeconds
) {
    /** 조회 결과가 없을 때 사용하는 기본값 인스턴스. */
    public static DashboardSummaryStats empty() {
        return new DashboardSummaryStats(0L, 0L, 0L, 0L, 0L, 0L);
    }
}
