package com.deepflow.infra.persistence.log;

import com.deepflow.application.port.out.persistence.FocusLogRepository;
import com.deepflow.domain.log.FocusLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FocusLogRepositoryImpl implements FocusLogRepository {

    private final FocusLogJpaRepository jpaRepository;

    @Override
    public Optional<FocusLog> findByFocusSessionId(Long sessionId) {
        return jpaRepository.findByFocusSessionId(sessionId);
    }
}
