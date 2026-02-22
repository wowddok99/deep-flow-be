package com.deepflow.application.event;

import com.deepflow.application.ai.AiSummaryService;
import com.deepflow.application.stats.DailyFocusStatsService;
import com.deepflow.domain.session.event.SessionStoppedEvent;
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

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionStoppedEvent(SessionStoppedEvent event) {
        log.info("Processing session stopped event: sessionId={}, userId={}, duration={}s",
                event.getSessionId(), event.getUserId(), event.getDurationSeconds());

        try {
            dailyFocusStatsService.upsertStats(event.getUserId(), event.getDurationSeconds());
        } catch (Exception e) {
            log.error("Failed to update daily stats for session {}", event.getSessionId(), e);
        }

        try {
            aiSummaryService.generateSummary(event.getSessionId());
        } catch (Exception e) {
            log.error("Failed to generate AI summary for session {}", event.getSessionId(), e);
        }

        log.info("Completed processing for session {}", event.getSessionId());
    }
}
