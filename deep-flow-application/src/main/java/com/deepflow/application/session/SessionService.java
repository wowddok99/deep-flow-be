package com.deepflow.application.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.exception.ResourceNotFoundException;
import com.deepflow.application.exception.session.SessionAlreadyExistsException;
import com.deepflow.application.exception.session.SessionNotDeletableException;
import com.deepflow.application.image.ImageService;
import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.application.session.dto.SessionDetailInfo;
import com.deepflow.application.session.dto.SessionInfo;
import com.deepflow.application.session.dto.SessionSummaryInfo;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import com.deepflow.domain.session.event.SessionStoppedEvent;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @DistributedLock(key = "'session_start:' + #userId")
    public SessionInfo startSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (sessionRepository.existsByUserIdAndStatus(user.getId(), SessionStatus.ONGOING)) {
            throw new SessionAlreadyExistsException();
        }

        FocusSession session = FocusSession.create(LocalDateTime.now(), user);
        SessionInfo info = SessionInfo.from(sessionRepository.save(session));
        log.info("세션 시작: sessionId={}, userId={}", info.id(), userId);
        return info;
    }

    public SliceResult<SessionSummaryInfo> getAllSessions(Long userId, Long cursorId, int size) {
        SliceResult<FocusSession> result = sessionRepository.findByUserIdWithLog(userId, cursorId, size);

        List<SessionSummaryInfo> content = result.content().stream()
                .map(SessionSummaryInfo::from)
                .toList();

        return new SliceResult<>(content, result.nextCursorId(), result.hasNext());
    }

    @Cacheable(value = "sessions", key = "#id")
    public SessionDetailInfo getSessionDetail(Long userId, Long id) {
        FocusSession session = sessionRepository.findByIdAndUserIdWithLogAndImages(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        return SessionDetailInfo.from(session);
    }

    @CacheEvict(value = "sessions", key = "#id")
    @Transactional
    public void updateLog(Long userId, Long id, String title, String content, String summary, List<String> imageUrls) {
        FocusSession session = getOwnedSession(id, userId);

        List<String> oldUrls = session.getFocusLog().getImages().stream()
                .map(img -> img.getImageUrl())
                .toList();

        session.getFocusLog().update(title, content, summary, imageUrls);

        imageService.deleteRemovedImages(oldUrls, imageUrls);
        log.debug("로그 수정: sessionId={}, userId={}", id, userId);
    }

    @CacheEvict(value = "sessions", key = "#id")
    @Transactional
    public void stopSession(Long userId, Long id) {
        FocusSession session = getOwnedSession(id, userId);
        session.stop(LocalDateTime.now());
        log.info("세션 종료: sessionId={}, userId={}, duration={}s", id, userId, session.getDurationSeconds());

        eventPublisher.publishEvent(new SessionStoppedEvent(
                session.getId(),
                session.getUser().getId(),
                session.getDurationSeconds()));
    }

    @CacheEvict(value = "sessions", key = "#id")
    @Transactional
    public void deleteSession(Long userId, Long id) {
        FocusSession session = getOwnedSession(id, userId);

        if (session.getStatus() == SessionStatus.ONGOING) {
            throw new SessionNotDeletableException();
        }

        session.softDelete();
        log.info("세션 삭제: sessionId={}, userId={}", id, userId);
    }

    private FocusSession getOwnedSession(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
    }
}