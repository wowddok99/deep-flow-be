package com.deepflow.application.session;

import com.deepflow.application.lock.DistributedLock;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
