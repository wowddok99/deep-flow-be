package com.deepflow.core.repository.session;

import com.deepflow.core.domain.session.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    List<FocusSession> findAllByOrderByStartTimeDesc();
}
