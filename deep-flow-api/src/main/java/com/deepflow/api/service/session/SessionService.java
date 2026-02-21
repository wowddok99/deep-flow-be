package com.deepflow.api.service.session;

import com.deepflow.api.dto.*;
import com.deepflow.api.exception.ResourceNotFoundException;
import com.deepflow.api.exception.session.SessionAlreadyExistsException;
import com.deepflow.api.exception.session.SessionNotDeletableException;
import com.deepflow.api.mapper.SessionMapper;
import com.deepflow.api.service.log.FocusLogService;
import com.deepflow.core.annotation.DistributedLock;
import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import com.deepflow.core.domain.session.event.SessionStoppedEvent;
import com.deepflow.core.domain.user.User;
import com.deepflow.core.repository.session.FocusSessionRepository;
import com.deepflow.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepflow.api.security.CustomUserDetails;
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
    private final SessionMapper sessionMapper;

    @Transactional
    @DistributedLock(key = "'session_start:' + #userId")
    public SessionResponse startSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (sessionRepository.existsByUserIdAndStatus(user.getId(), SessionStatus.ONGOING)) {
            throw new SessionAlreadyExistsException();
        }

        FocusSession session = FocusSession.create(LocalDateTime.now(), user);
        return sessionMapper.toSessionResponse(sessionRepository.save(session));
    }

    // 커서 기반 페이지네이션으로 세션 목록 조회
    public CursorResponse<SessionSummaryResponse> getAllSessions(Long cursorId, int size) {
        Long userId = getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(0, size);

        // 첫 페이지는 cursorId 없이, 이후는 커서 기준으로 그 다음 데이터 조회
        Slice<FocusSession> slice = (cursorId == null)
                ? sessionRepository.findAllByUserIdWithLog(userId, pageRequest)
                : sessionRepository.findByUserIdAndIdLessThanWithLog(userId, cursorId, pageRequest);

        List<SessionSummaryResponse> content = slice.getContent().stream()
                .map(sessionMapper::toSessionSummaryResponse)
                .toList();

        // 마지막 항목의 ID를 다음 커서로 사용
        Long nextCursorId = content.isEmpty() ? null : content.get(content.size() - 1).id();
        return new CursorResponse<>(content, nextCursorId, slice.hasNext());
    }

    @Cacheable(value = "sessions", key = "#id")
    public SessionDetailResponse getSessionDetail(Long id) {
        FocusSession session = sessionRepository.findByIdAndUserIdWithLogAndImages(id, getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        return sessionMapper.toSessionDetailResponse(session);
    }

    @Transactional
    @CacheEvict(value = "sessions", key = "#id")
    public void updateLog(Long id, LogUpdateRequest request) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());

        String contentJson = request.content() != null ? request.content().toString() : null;

        focusLogService.updateLogDetails(
                session.getFocusLog(),
                request.title(),
                contentJson,
                request.summary(),
                request.imageUrls());
    }

    @Transactional
    @CacheEvict(value = "sessions", key = "#id")
    public void stopSession(Long id) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());
        session.stop(LocalDateTime.now());

        // SessionEventListener가 AFTER_COMMIT 시점에 비동기로 수신
        eventPublisher.publishEvent(new SessionStoppedEvent(
                session.getId(),
                session.getUser().getId(),
                session.getDurationSeconds()));
    }

    @Transactional
    @CacheEvict(value = "sessions", key = "#id")
    public void deleteSession(Long id) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());

        if (session.getStatus() == SessionStatus.ONGOING) {
            throw new SessionNotDeletableException();
        }

        session.softDelete();
    }

    private FocusSession getOwnedSession(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        return details.getUserId();
    }
}
