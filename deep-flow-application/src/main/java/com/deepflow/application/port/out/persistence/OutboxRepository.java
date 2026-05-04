package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.outbox.OutboxEvent;

import java.util.List;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent event);

    /**
     * status=PENDING 인 row 를 created_at 오름차순으로 batchSize 만큼 조회 (FOR UPDATE 락 옵션은 워커가 단일 인스턴스 가정으로 생략).
     */
    List<OutboxEvent> findPending(int batchSize);
}
