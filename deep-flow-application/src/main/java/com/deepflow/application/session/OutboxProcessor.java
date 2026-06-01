package com.deepflow.application.session;

import com.deepflow.application.port.out.persistence.OutboxRepository;
import com.deepflow.application.port.out.search.SessionIndexer;
import com.deepflow.application.session.dto.SessionSharedPayload;
import com.deepflow.application.session.dto.SessionUnsharedPayload;
import com.deepflow.domain.outbox.OutboxEvent;
import com.deepflow.domain.outbox.OutboxEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final SessionIndexer sessionIndexer;
    private final ObjectMapper objectMapper;

    @Value("${app.outbox.worker.batch-size:50}")
    private int batchSize;

    @Value("${app.outbox.worker.max-retry:3}")
    private int maxRetry;

    /**
     * 저장된 아웃박스 이벤트를 검색 인덱스에 반영
     *
     * 처리 실패 이벤트는 재시도 한도까지 남겨 다음 배치에서 다시 처리
     */
    @Transactional
    public int processBatch() {
        List<OutboxEvent> events = outboxRepository.findPending(batchSize);
        if (events.isEmpty()) return 0;

        int processed = 0;
        for (OutboxEvent event : events) {
            try {
                handle(event);
                event.markSuccess(LocalDateTime.now());
                processed++;
            } catch (Exception e) {
                event.markFailure(e.getMessage(), maxRetry);
                log.warn("outbox 처리 실패: id={}, retry={}, type={}, msg={}",
                        event.getId(), event.getRetryCount(), event.getEventType(), e.getMessage());
            }
        }
        return processed;
    }

    private void handle(OutboxEvent event) throws Exception {
        OutboxEventType type = event.getEventType();
        switch (type) {
            // 태그 변경도 검색 결과에 노출되므로 공유 이벤트와 같은 색인 경로 사용
            case SESSION_SHARED, SESSION_TAGS_UPDATED ->
                    sessionIndexer.index(objectMapper.readValue(event.getPayload(), SessionSharedPayload.class));
            case SESSION_UNSHARED -> {
                SessionUnsharedPayload p = objectMapper.readValue(event.getPayload(), SessionUnsharedPayload.class);
                sessionIndexer.delete(p.sessionId());
            }
            default -> throw new IllegalStateException("Unknown outbox event type: " + type);
        }
    }
}
