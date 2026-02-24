package com.deepflow.application.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.exception.ResourceNotFoundException;
import com.deepflow.application.exception.session.SessionAlreadyExistsException;
import com.deepflow.application.exception.session.SessionNotDeletableException;
import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.session.dto.SessionDetailInfo;
import com.deepflow.application.session.dto.SessionInfo;
import com.deepflow.application.session.dto.SessionSummaryInfo;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.FocusSessionRepository;
import com.deepflow.domain.session.SessionStatus;
import com.deepflow.domain.session.event.SessionStoppedEvent;
import com.deepflow.domain.user.User;
import com.deepflow.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
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
        return SessionInfo.from(sessionRepository.save(session));
    }

    public SliceResult<SessionSummaryInfo> getAllSessions(Long userId, Long cursorId, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);

        Slice<FocusSession> slice = (cursorId == null)
                ? sessionRepository.findAllByUserIdWithLog(userId, pageRequest)
                : sessionRepository.findByUserIdAndIdLessThanWithLog(userId, cursorId, pageRequest);

        List<SessionSummaryInfo> content = slice.getContent().stream()
                .map(SessionSummaryInfo::from)
                .toList();

        Long nextCursorId = slice.hasNext()
                ? slice.getContent().get(slice.getContent().size() - 1).getId()
                : null;

        return new SliceResult<>(content, nextCursorId, slice.hasNext());
    }

    public SessionDetailInfo getSessionDetail(Long userId, Long id) {
        FocusSession session = sessionRepository.findByIdAndUserIdWithLogAndImages(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        return SessionDetailInfo.from(session);
    }

    @Transactional
    public void updateLog(Long userId, Long id, String title, String content, String summary, List<String> imageUrls) {
        FocusSession session = getOwnedSession(id, userId);
        session.getFocusLog().update(title, content, summary, imageUrls);
    }

    @Transactional
    public void stopSession(Long userId, Long id) {
        FocusSession session = getOwnedSession(id, userId);
        session.stop(LocalDateTime.now());

        eventPublisher.publishEvent(new SessionStoppedEvent(
                session.getId(),
                session.getUser().getId(),
                session.getDurationSeconds()));
    }

    @Transactional
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
