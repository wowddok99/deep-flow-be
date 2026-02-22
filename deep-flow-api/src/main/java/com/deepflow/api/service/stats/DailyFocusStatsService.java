package com.deepflow.api.service.stats;

import com.deepflow.api.dto.DailyStatsResponse;
import com.deepflow.api.dto.StatsOverviewResponse;
import com.deepflow.core.domain.stats.DailyFocusStats;
import com.deepflow.core.repository.stats.DailyFocusStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyFocusStatsService {

    private final DailyFocusStatsRepository dailyFocusStatsRepository;

    @Transactional
    public void upsertStats(Long userId, long durationSeconds) {
        LocalDate today = LocalDate.now();

        Optional<DailyFocusStats> existingStats =
                dailyFocusStatsRepository.findByUserIdAndDate(userId, today);

        if (existingStats.isPresent()) {
            existingStats.get().addSession(durationSeconds);
        } else {
            DailyFocusStats stats = DailyFocusStats.builder()
                    .userId(userId)
                    .date(today)
                    .totalSessions(1)
                    .totalDurationSeconds(durationSeconds)
                    .build();
            dailyFocusStatsRepository.save(stats);
        }

        log.info("Updated daily stats for user {} on {}", userId, today);
    }

    public StatsOverviewResponse getOverview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        DailyFocusStats todayStats = dailyFocusStatsRepository.findByUserIdAndDate(userId, today)
                .orElse(null);

        int todaySessions = todayStats != null ? todayStats.getTotalSessions() : 0;
        long todayDuration = todayStats != null ? todayStats.getTotalDurationSeconds() : 0;

        int weekSessions = dailyFocusStatsRepository.sumSessionsByUserIdAndDateBetween(userId, weekStart, today);
        long weekDuration = dailyFocusStatsRepository.sumDurationByUserIdAndDateBetween(userId, weekStart, today);

        return new StatsOverviewResponse(
                todaySessions,
                todayDuration,
                weekSessions,
                weekDuration
        );
    }

    public List<DailyStatsResponse> getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        return dailyFocusStatsRepository
                .findByUserIdAndDateBetweenOrderByDateAsc(userId, weekStart, today)
                .stream()
                .map(s -> new DailyStatsResponse(s.getDate(), s.getTotalSessions(), s.getTotalDurationSeconds()))
                .toList();
    }
}
