package com.deepflow.infra.persistence.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionJpaRepository jpaRepository;

    @Override
    public FocusSession save(FocusSession session) {
        return jpaRepository.save(session);
    }

    @Override
    public boolean existsByUserIdAndStatus(Long userId, SessionStatus status) {
        return jpaRepository.existsByUserIdAndStatus(userId, status);
    }

    @Override
    public Optional<FocusSession> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public SliceResult<FocusSession> findByUserIdWithLog(Long userId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size);

        Slice<FocusSession> slice;
        if (cursorId == null) {
            slice = jpaRepository.findAllByUserIdWithLog(userId, pageable);
        } else {
            slice = jpaRepository.findByUserIdAndIdLessThanWithLog(userId, cursorId, pageable);
        }

        List<FocusSession> content = slice.getContent();
        Long nextCursorId = slice.hasNext()
                ? content.get(content.size() - 1).getId()
                : null;

        return new SliceResult<>(content, nextCursorId, slice.hasNext());
    }

    @Override
    public Optional<FocusSession> findByIdAndUserIdWithLogAndImages(Long id, Long userId) {
        return jpaRepository.findByIdAndUserIdWithLogAndImages(id, userId);
    }
}
