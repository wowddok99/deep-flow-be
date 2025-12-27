package com.deepflow.core.repository.session;

import com.deepflow.core.domain.log.FocusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusLogRepository extends JpaRepository<FocusLog, Long> {
}
