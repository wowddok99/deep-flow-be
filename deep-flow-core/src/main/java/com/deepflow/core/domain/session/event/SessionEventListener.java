package com.deepflow.core.domain.session.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionEventListener {

    @Async("threadPoolTaskExecutor")
    @EventListener
    public void handleSessionStoppedEvent(SessionStoppedEvent event) {
        log.info("Async processing for session id: {}", event.getSessionId());
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Completed async processing for session {}. Duration: {}s", 
            event.getSessionId(), event.getDurationSeconds());
    }
}
