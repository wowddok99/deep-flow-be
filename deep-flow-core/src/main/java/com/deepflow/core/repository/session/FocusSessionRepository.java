package com.deepflow.core.repository.session;

import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    Slice<FocusSession> findAllByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Slice<FocusSession> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long id, Pageable pageable);
    
    // User-scoped checks
    boolean existsByUserIdAndStatus(Long userId, SessionStatus status);
    Optional<FocusSession> findByIdAndUserId(Long id, Long userId);
}
