package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.CrewActivityInfo;

import java.util.List;

public record CrewActivityResponse(
        int activeNowCount,
        int todayParticipantCount,
        long todayTotalDurationSeconds,
        List<WeeklyTrendPointResponse> weeklyTrend,
        List<MemberRankingResponse> memberRanking
) {
    public record WeeklyTrendPointResponse(String date, long totalDurationSeconds) {}
    public record MemberRankingResponse(Long userId, String name, long totalDurationSeconds) {}

    public static CrewActivityResponse from(CrewActivityInfo info) {
        return new CrewActivityResponse(
                info.activeNowCount(),
                info.todayParticipantCount(),
                info.todayTotalDurationSeconds(),
                info.weeklyTrend().stream()
                        .map(p -> new WeeklyTrendPointResponse(p.date(), p.totalDurationSeconds()))
                        .toList(),
                info.memberRanking().stream()
                        .map(r -> new MemberRankingResponse(r.userId(), r.name(), r.totalDurationSeconds()))
                        .toList()
        );
    }
}
