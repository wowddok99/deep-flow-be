package com.deepflow.infra.persistence.log;

import com.deepflow.domain.log.FocusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface FocusLogJpaRepository extends JpaRepository<FocusLog, Long> {

    @Query("SELECT fl FROM FocusSession fs JOIN fs.focusLog fl WHERE fs.id = :sessionId")
    Optional<FocusLog> findByFocusSessionId(@Param("sessionId") Long sessionId);
}
