package com.deepflow.api.event;

import com.deepflow.api.service.ai.AiSummaryService;
import com.deepflow.api.service.stats.DailyFocusStatsService;
import com.deepflow.core.domain.session.event.SessionStoppedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventListener {

    private final DailyFocusStatsService dailyFocusStatsService;
    private final AiSummaryService aiSummaryService;

    /**
     * 세션 종료 시 통계 갱신 및 AI 요약을 비동기로 처리합니다.
     * (메인 로직에 영향을 주지 않도록 각각 독립적으로 실행)
     */
    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionStoppedEvent(SessionStoppedEvent event) {
        log.info("Processing session stopped event: sessionId={}, userId={}, duration={}s",
                event.getSessionId(), event.getUserId(), event.getDurationSeconds());

        // 1. 일일 집중 통계 갱신
        try {
            dailyFocusStatsService.upsertStats(event.getUserId(), event.getDurationSeconds());
        } catch (Exception e) {
            log.error("Failed to update daily stats for session {}", event.getSessionId(), e);
        }

        // 2. AI 자동 요약 생성
        try {
            aiSummaryService.generateSummary(event.getSessionId());
        } catch (Exception e) {
            log.error("Failed to generate AI summary for session {}", event.getSessionId(), e);
        }

        log.info("Completed processing for session {}", event.getSessionId());
    }
}
