package com.deepflow.core.repository.session;

import com.deepflow.core.domain.session.FocusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusLogRepository extends JpaRepository<FocusLog, Long> {
}
