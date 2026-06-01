package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.outbox.OutboxEvent;

import java.util.List;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent event);

    /**
     * 단일 워커 전제에서 오래된 PENDING 이벤트부터 처리하기 위해 createdAt 오름차순 조회
     */
    List<OutboxEvent> findPending(int batchSize);
}
