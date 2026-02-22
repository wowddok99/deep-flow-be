package com.deepflow.application.session;

import com.deepflow.application.exception.ResourceNotFoundException;
import com.deepflow.application.exception.session.SessionAlreadyExistsException;
import com.deepflow.application.exception.session.SessionNotDeletableException;
import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.log.FocusLogService;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.FocusSessionRepository;
import com.deepflow.domain.session.SessionStatus;
import com.deepflow.domain.session.event.SessionStoppedEvent;
import com.deepflow.domain.user.User;
import com.deepflow.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final FocusSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final FocusLogService focusLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @DistributedLock(key = "'session_start:' + #userId")
    public FocusSession startSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (sessionRepository.existsByUserIdAndStatus(user.getId(), SessionStatus.ONGOING)) {
            throw new SessionAlreadyExistsException();
        }

        FocusSession session = FocusSession.create(LocalDateTime.now(), user);
        return sessionRepository.save(session);
    }

    public Slice<FocusSession> getAllSessions(Long userId, Long cursorId, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);

        return (cursorId == null)
                ? sessionRepository.findAllByUserIdWithLog(userId, pageRequest)
                : sessionRepository.findByUserIdAndIdLessThanWithLog(userId, cursorId, pageRequest);
    }

    @Cacheable(value = "sessions", key = "#id")
    public FocusSession getSessionDetail(Long userId, Long id) {
        return sessionRepository.findByIdAndUserIdWithLogAndImages(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "sessions", key = "#id")
    public void updateLog(Long userId, Long id, String title, String content, String summary, List<String> imageUrls) {
        FocusSession session = getOwnedSession(id, userId);
        focusLogService.updateLogDetails(session.getFocusLog(), title, content, summary, imageUrls);
    }

    @Transactional
    @CacheEvict(value = "sessions", key = "#id")
    public void stopSession(Long userId, Long id) {
        FocusSession session = getOwnedSession(id, userId);
        session.stop(LocalDateTime.now());

        eventPublisher.publishEvent(new SessionStoppedEvent(
                session.getId(),
                session.getUser().getId(),
                session.getDurationSeconds()));
    }

    @Transactional
    @CacheEvict(value = "sessions", key = "#id")
    public void deleteSession(Long userId, Long id) {
        FocusSession session = getOwnedSession(id, userId);

        if (session.getStatus() == SessionStatus.ONGOING) {
            throw new SessionNotDeletableException();
        }

        session.softDelete();
    }

    private FocusSession getOwnedSession(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
    }
}
