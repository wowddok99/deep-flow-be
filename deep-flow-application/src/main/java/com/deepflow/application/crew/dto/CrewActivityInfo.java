package com.deepflow.application.crew.dto;

import java.util.List;

public record CrewActivityInfo(
        int activeNowCount,
        int todayParticipantCount,
        long todayTotalDurationSeconds,
        List<WeeklyTrendPoint> weeklyTrend,
        List<MemberRankingEntry> memberRanking
) {
    public record WeeklyTrendPoint(String date, long totalDurationSeconds) {}

    public record MemberRankingEntry(Long userId, String name, long totalDurationSeconds) {}
}
