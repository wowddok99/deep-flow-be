package com.deepflow.core.repository.session;

import com.deepflow.core.domain.log.FocusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FocusLogRepository extends JpaRepository<FocusLog, Long> {

    @Query("SELECT fl FROM FocusSession fs JOIN fs.focusLog fl WHERE fs.id = :sessionId")
    Optional<FocusLog> findByFocusSessionId(@Param("sessionId") Long sessionId);
}
