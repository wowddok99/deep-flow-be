package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;

import java.util.Optional;

public interface SessionRepository {

    FocusSession save(FocusSession session);

    boolean existsByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<FocusSession> findByIdAndUserId(Long id, Long userId);

    SliceResult<FocusSession> findByUserIdWithLog(Long userId, Long cursorId, int size);

    Optional<FocusSession> findByIdAndUserIdWithLogAndImages(Long id, Long userId);
}
