package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.SessionAlreadySharedException;
import com.deepflow.application.exception.session.SessionNotFoundException;
import com.deepflow.application.exception.session.SessionNotShareableException;
import com.deepflow.application.exception.session.SessionNotSharedException;
import com.deepflow.application.exception.session.TagLimitExceededException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.session.dto.ShareSessionCommand;
import com.deepflow.application.session.dto.SharedSessionInfo;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.event.SessionSharedEvent;
import com.deepflow.domain.session.event.SessionUnsharedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionShareService {

    public static final int MAX_TAGS = 5;

    private final SessionRepository sessionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final SessionTagRepository tagRepository;
    private final TagNormalizer tagNormalizer;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * SessionShareLocker 를 통해서만 호출. 외부 진입 금지 (락 우회 위험).
     * 분산 락 안에서 REQUIRES_NEW TX 가 시작/커밋된다.
     */
    @Transactional
    public SharedSessionInfo shareLockedInternal(Long userId, Long sessionId, ShareSessionCommand cmd) {
        FocusSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);

        if (session.isShared()) throw new SessionAlreadySharedException();
        if (!session.isShareable()) throw new SessionNotShareableException();

        if (!crewMemberRepository.existsByCrewIdAndUserId(cmd.crewId(), userId)) {
            throw new NotCrewMemberException();
        }

        List<String> normalized = normalizeTags(cmd.tags());

        session.shareTo(cmd.crewId(), LocalDateTime.now());
        sessionRepository.save(session);
        tagRepository.replaceAll(sessionId, normalized);

        eventPublisher.publishEvent(new SessionSharedEvent(sessionId, cmd.crewId(), userId, normalized));
        log.info("세션 공유: sessionId={}, crewId={}, tags={}", sessionId, cmd.crewId(), normalized);

        return SharedSessionInfo.from(session, normalized);
    }

    @Transactional
    public void unshareLockedInternal(Long userId, Long sessionId) {
        FocusSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);

        if (!session.isShared()) throw new SessionNotSharedException();

        Long crewId = session.getSharedCrewId();
        session.unshare();
        sessionRepository.save(session);
        // 철회 시 태그 row 도 삭제. 재공유는 새 태그를 입력하는 흐름이라 이전 태그 보존은 stale.
        tagRepository.deleteAllBySessionId(sessionId);

        eventPublisher.publishEvent(new SessionUnsharedEvent(sessionId, crewId, userId));
        log.info("세션 공유 철회: sessionId={}, crewId={}", sessionId, crewId);
    }

    @Transactional
    public SharedSessionInfo updateTagsLockedInternal(Long userId, Long sessionId, List<String> rawTags) {
        FocusSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);

        if (!session.isShared()) throw new SessionNotSharedException();

        List<String> normalized = normalizeTags(rawTags);
        tagRepository.replaceAll(sessionId, normalized);

        log.info("세션 태그 갱신: sessionId={}, tags={}", sessionId, normalized);
        return SharedSessionInfo.from(session, normalized);
    }

    private List<String> normalizeTags(List<String> raw) {
        if (raw == null) return List.of();
        if (raw.size() > MAX_TAGS) throw new TagLimitExceededException();

        List<String> normalized = raw.stream()
                .map(tagNormalizer::normalize)
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();

        if (normalized.size() > MAX_TAGS) throw new TagLimitExceededException();
        return normalized;
    }
}
