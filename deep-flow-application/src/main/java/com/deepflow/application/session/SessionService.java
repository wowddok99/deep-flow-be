package com.deepflow.application.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.exception.ResourceNotFoundException;
import com.deepflow.application.exception.session.SessionAlreadyExistsException;
import com.deepflow.application.exception.session.SessionNotDeletableException;
import com.deepflow.application.image.ImageService;
import com.deepflow.application.achievement.SessionTimeScheduler;
import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.application.session.dto.SessionDetailInfo;
import com.deepflow.application.session.dto.SessionInfo;
import com.deepflow.application.session.dto.SessionSummaryInfo;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import com.deepflow.domain.session.event.LogUpdatedEvent;
import com.deepflow.domain.session.event.SessionStartedEvent;
import com.deepflow.domain.session.event.SessionStoppedEvent;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final SessionTimeScheduler sessionTimeScheduler;

    @Transactional
    @DistributedLock(key = "'session_start:' + #userId")
    public SessionInfo startSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (sessionRepository.existsByUserIdAndStatus(user.getId(), SessionStatus.ONGOING)) {
            throw new SessionAlreadyExistsException();
        }

        FocusSession session = FocusSession.create(LocalDateTime.now(), user);
        SessionInfo sessionInfo = SessionInfo.from(sessionRepository.save(session));

        log.info("세션 시작: sessionId={}, userId={}", sessionInfo.id(), userId);

        sessionTimeScheduler.scheduleForSession(userId, sessionInfo.id());
        eventPublisher.publishEvent(new SessionStartedEvent(sessionInfo.id(), userId));

        return sessionInfo;
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
    public void updateLog(Long userId, Long sessionId, String title, String content, String summary, List<String> imageUrls) {
        FocusSession session = getOwnedSession(sessionId, userId);

        List<String> oldImageUrls = session.getFocusLog().getImages().stream()
                .map(img -> img.getImageUrl())
                .toList();

        session.getFocusLog().update(title, content, summary, imageUrls);

        imageService.deleteRemovedImages(oldImageUrls, imageUrls);

        // 로그 수정 후 커밋 기준으로 칭호를 평가하기 위해 이벤트로 분리
        eventPublisher.publishEvent(new LogUpdatedEvent(session.getId(), userId));

        log.debug("로그 수정: sessionId={}, userId={}", sessionId, userId);
    }

    @Caching(evict = {
            @CacheEvict(value = "sessions", key = "#id"),
            @CacheEvict(value = "hourlyDistribution", key = "#userId")
    })
    @Transactional
    public void stopSession(Long userId, Long id) {
        FocusSession session = getOwnedSession(id, userId);

        session.stop(LocalDateTime.now());

        // 종료된 세션은 더 이상 시간 기반 칭호 예약이 필요 없으므로 남은 작업 취소
        sessionTimeScheduler.cancelForSession(id);

        log.info("세션 종료: sessionId={}, userId={}, duration={}s",
                id, userId, session.getDurationSeconds());

        SessionStoppedEvent event = new SessionStoppedEvent(
                session.getId(),
                session.getUser().getId(),
                session.getDurationSeconds(),
                session.getStartTime(),
                session.getEndTime());

        eventPublisher.publishEvent(event);
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
