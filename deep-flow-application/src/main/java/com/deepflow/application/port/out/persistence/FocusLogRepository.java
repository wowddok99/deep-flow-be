package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.log.FocusLog;

import java.util.Optional;

public interface FocusLogRepository {

    Optional<FocusLog> findByFocusSessionId(Long sessionId);
}
