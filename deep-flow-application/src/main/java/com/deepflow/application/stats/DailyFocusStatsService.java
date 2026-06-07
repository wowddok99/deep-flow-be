package com.deepflow.application.stats;

import com.deepflow.application.port.out.persistence.StatsRepository;
import com.deepflow.application.stats.dto.DailyStatsInfo;
import com.deepflow.domain.stats.DailyFocusStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyFocusStatsService {

    private final StatsRepository statsRepository;

    /**
     * 세션 집중 시간을 날짜별 통계에 반영
     *
     * 자정을 넘긴 세션은 날짜별 집중 시간으로 분리하고
     * 세션 횟수는 시작일에만 반영해 단일 세션이 여러 번 집계되지 않도록 처리
     */
    @Transactional
    public void upsertStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime currentStart = startTime;
        boolean isFirstDay = true;

        while (currentStart.toLocalDate().isBefore(endTime.toLocalDate())) {
            LocalDateTime nextMidnight = currentStart.toLocalDate().plusDays(1).atStartOfDay();
            long seconds = Duration.between(currentStart, nextMidnight).getSeconds();

            if (seconds > 0) {
                upsertDailyStats(userId, currentStart.toLocalDate(), isFirstDay ? 1 : 0, seconds);
            }

            currentStart = nextMidnight;
            isFirstDay = false;
        }

        long seconds = Duration.between(currentStart, endTime).getSeconds();
        if (seconds > 0) {
            upsertDailyStats(userId, currentStart.toLocalDate(), isFirstDay ? 1 : 0, seconds);
        }
    }

    private void upsertDailyStats(Long userId, LocalDate date, int sessionDelta, long durationSeconds) {
        Optional<DailyFocusStats> existingStats = statsRepository.findByUserIdAndDate(userId, date);

        if (existingStats.isPresent()) {
            DailyFocusStats stats = existingStats.get();

            if (sessionDelta > 0) {
                stats.addSession(durationSeconds);
            } else {
                // 자정 이후 구간은 같은 세션이므로 세션 횟수 없이 시간만 반영
                stats.addDuration(durationSeconds);
            }
        } else {
            DailyFocusStats stats = DailyFocusStats.builder()
                    .userId(userId)
                    .date(date)
                    .totalSessions(sessionDelta)
                    .totalDurationSeconds(durationSeconds)
                    .build();
            statsRepository.save(stats);
        }

        log.info("Updated daily stats for user {} on {}", userId, date);
    }

    public StatsOverview getOverview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        DailyFocusStats todayStats = statsRepository.findByUserIdAndDate(userId, today)
                .orElse(null);

        int todaySessions = todayStats != null ? todayStats.getTotalSessions() : 0;
        long todayDuration = todayStats != null ? todayStats.getTotalDurationSeconds() : 0;

        int weekSessions = statsRepository.sumSessionsByUserIdAndDateBetween(userId, weekStart, today);
        long weekDuration = statsRepository.sumDurationByUserIdAndDateBetween(userId, weekStart, today);

        return new StatsOverview(todaySessions, todayDuration, weekSessions, weekDuration);
    }

    public List<DailyStatsInfo> getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        return statsRepository
                .findByUserIdAndDateBetween(userId, weekStart, today)
                .stream()
                .map(DailyStatsInfo::from)
                .toList();
    }

    public record StatsOverview(int todaySessions, long todayDurationSeconds,
                                int weekSessions, long weekDurationSeconds) {
    }
}
