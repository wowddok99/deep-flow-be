package com.deepflow.infra.persistence.outbox;

import com.deepflow.domain.outbox.OutboxEvent;
import com.deepflow.domain.outbox.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface OutboxJpaRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC, o.id ASC")
    List<OutboxEvent> findByStatusOrdered(@Param("status") OutboxStatus status, Pageable pageable);
}
