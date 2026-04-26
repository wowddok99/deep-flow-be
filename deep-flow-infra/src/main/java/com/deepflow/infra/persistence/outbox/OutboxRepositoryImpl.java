package com.deepflow.infra.persistence.outbox;

import com.deepflow.application.port.out.persistence.OutboxRepository;
import com.deepflow.domain.outbox.OutboxEvent;
import com.deepflow.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxJpaRepository jpa;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return jpa.save(event);
    }

    @Override
    public List<OutboxEvent> findPending(int batchSize) {
        return jpa.findByStatusOrdered(OutboxStatus.PENDING, PageRequest.of(0, batchSize));
    }
}
