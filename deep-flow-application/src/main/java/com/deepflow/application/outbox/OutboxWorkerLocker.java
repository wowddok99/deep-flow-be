package com.deepflow.application.outbox;

import com.deepflow.application.lock.DistributedLock;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 아웃박스 배치 실행 임계구역 전용 빈
 *
 * 여러 인스턴스가 동시에 같은 이벤트를 처리하지 않도록 분산 락 경계로 분리
 */
@Component
public class OutboxWorkerLocker {

    private final OutboxProcessor processor;

    public OutboxWorkerLocker(@Lazy OutboxProcessor processor) {
        this.processor = processor;
    }

    @DistributedLock(key = "'outbox_worker'", waitTime = 0)
    public int runOnce() {
        return processor.processBatch();
    }
}
