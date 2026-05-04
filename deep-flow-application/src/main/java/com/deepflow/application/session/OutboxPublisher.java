package com.deepflow.application.session;

import com.deepflow.application.port.out.persistence.OutboxRepository;
import com.deepflow.domain.outbox.OutboxEvent;
import com.deepflow.domain.outbox.OutboxEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String AGGREGATE_SESSION = "SESSION";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(OutboxEventType type, Long aggregateId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(OutboxEvent.create(AGGREGATE_SESSION, aggregateId, type, json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload serialization failed", e);
        }
    }
}
