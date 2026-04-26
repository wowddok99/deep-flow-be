package com.deepflow.domain.outbox;

import com.deepflow.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "outbox_event",
        indexes = {
                @Index(name = "idx_outbox_event_status_created", columnList = "status, created_at")
        }
)
public class OutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 30)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private OutboxEventType eventType;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static OutboxEvent create(String aggregateType, Long aggregateId, OutboxEventType type, String payload) {
        return OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(type)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
    }

    public void markSuccess(LocalDateTime now) {
        this.status = OutboxStatus.SUCCESS;
        this.processedAt = now;
        this.lastError = null;
    }

    public void markFailure(String error, int maxRetry) {
        this.retryCount += 1;
        this.lastError = (error == null || error.length() <= 500) ? error : error.substring(0, 500);
        if (this.retryCount >= maxRetry) {
            this.status = OutboxStatus.DEAD;
        }
    }
}
