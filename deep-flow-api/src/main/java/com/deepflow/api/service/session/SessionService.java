package com.deepflow.api.service.session;

import com.deepflow.api.dto.*;
import com.deepflow.api.dto.CursorResponse;
import com.deepflow.api.exception.ResourceNotFoundException;
import com.deepflow.api.service.log.FocusLogService;
import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import com.deepflow.core.domain.user.User;
import com.deepflow.core.repository.session.FocusSessionRepository;
import com.deepflow.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional
    public SessionResponse startSession() {
        User user = getCurrentUserEntity();

        if (sessionRepository.existsByUserIdAndStatus(user.getId(), SessionStatus.ONGOING)) {
            throw new IllegalStateException("An ongoing session already exists");
        }

        FocusSession session = FocusSession.create(LocalDateTime.now(), user);
        return SessionResponse.from(sessionRepository.save(session));
    }

    public CursorResponse<SessionSummaryResponse> getAllSessions(Long cursorId, int size) {
        Long userId = getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(0, size);

        Slice<FocusSession> slice = (cursorId == null) ?
            sessionRepository.findAllByUserIdOrderByIdDesc(userId, pageRequest) :
            sessionRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursorId, pageRequest);

        List<SessionSummaryResponse> content = slice.getContent().stream()
                .map(SessionSummaryResponse::from)
                .toList();

        Long nextCursorId = content.isEmpty() ? null : content.get(content.size() - 1).id();

        return new CursorResponse<>(content, nextCursorId, slice.hasNext());
    }

    public SessionDetailResponse getSessionDetail(Long id) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());
        return SessionDetailResponse.from(session);
    }

    @Transactional
    public void updateLog(Long id, LogUpdateRequest request) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());

        focusLogService.updateLogDetails(
                session.getFocusLog(),
                request.title(),
                request.content(),
                request.summary(),
                request.imageUrls());
    }

    @Transactional
    public void stopSession(Long id) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());
        session.stop(LocalDateTime.now());
    }

    @Transactional
    public void deleteSession(Long id) {
        FocusSession session = getOwnedSession(id, getCurrentUserId());
        sessionRepository.delete(session);
    }


    private FocusSession getOwnedSession(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
    }

    // DB 조회 없이 SecurityContext에서 ID만 꺼내는 메서드
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.deepflow.api.security.CustomUserDetails details) {
             return details.getUserId();
        }
        return getCurrentUserEntity().getId();
    }

    // DB에서 User 엔티티 조회하는 메서드
    private User getCurrentUserEntity() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
