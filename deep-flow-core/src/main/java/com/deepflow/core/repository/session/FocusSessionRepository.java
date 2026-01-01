package com.deepflow.core.repository.session;

import com.deepflow.core.domain.session.FocusSession;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    Slice<FocusSession> findAllByOrderByIdDesc(Pageable pageable);
    Slice<FocusSession> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);
}
