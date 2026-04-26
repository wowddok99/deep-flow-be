package com.deepflow.application.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.outbox.worker.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxWorker {

    private final OutboxWorkerLocker locker;

    @Scheduled(fixedDelay = 1000L)
    public void run() {
        try {
            int processed = locker.runOnce();
            if (processed > 0) {
                log.debug("outbox processed batch: count={}", processed);
            }
        } catch (Exception e) {
            log.debug("outbox worker tick skipped: {}", e.getMessage());
        }
    }
}
