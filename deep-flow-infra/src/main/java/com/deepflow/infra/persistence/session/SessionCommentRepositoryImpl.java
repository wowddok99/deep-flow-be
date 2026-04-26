package com.deepflow.infra.persistence.session;

import com.deepflow.application.port.out.persistence.SessionCommentRepository;
import com.deepflow.domain.session.comment.SessionComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SessionCommentRepositoryImpl implements SessionCommentRepository {

    private final SessionCommentJpaRepository jpa;

    @Override
    public SessionComment save(SessionComment comment) {
        return jpa.save(comment);
    }

    @Override
    public Optional<SessionComment> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<SessionComment> findAllBySessionIdWithUser(Long sessionId) {
        return jpa.findAllBySessionIdWithUser(sessionId);
    }

    @Override
    public Map<Long, Integer> countBySessionIds(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return Map.of();
        Map<Long, Integer> result = new HashMap<>();
        for (Object[] row : jpa.countGroupedBySessionIds(sessionIds)) {
            result.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return result;
    }
}
