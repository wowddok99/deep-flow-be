package com.deepflow.application.session.share;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.exception.session.SessionAlreadySharedException;
import com.deepflow.application.exception.session.SessionNotFoundException;
import com.deepflow.application.exception.session.SessionNotShareableException;
import com.deepflow.application.exception.session.SessionNotSharedException;
import com.deepflow.application.exception.session.TagLimitExceededException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.outbox.OutboxPublisher;
import com.deepflow.application.session.share.dto.SessionSharedPayload;
import com.deepflow.application.session.share.dto.SessionUnsharedPayload;
import com.deepflow.application.session.share.dto.ShareSessionCommand;
import com.deepflow.application.session.share.dto.SharedSessionInfo;
import com.deepflow.application.session.tag.TagNormalizer;
import com.deepflow.domain.outbox.OutboxEventType;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.event.SessionSharedEvent;
import com.deepflow.domain.session.event.SessionUnsharedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
    private final OutboxPublisher outboxPublisher;

    /**
     * SessionShareLocker 가 분산 락을 획득한 뒤에만 호출되는 공유 본문
     *
     * 외부에서 직접 호출하면 중복 공유 검증을 락 없이 통과할 수 있으므로 SessionShareLocker 를 통해 호출
     */
    @CacheEvict(value = "sessions", key = "#sessionId", beforeInvocation = true)
    @Transactional
    public SharedSessionInfo shareLockedInternal(Long userId, Long sessionId, ShareSessionCommand cmd) {
        // 다른 유저의 세션 존재 여부가 노출되지 않도록 권한과 존재 확인을 한 쿼리로 처리
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

        // 검색 인덱싱은 아웃박스로 넘겨 공유 저장 트랜잭션과 외부 검색 반영을 분리
        outboxPublisher.publish(OutboxEventType.SESSION_SHARED, sessionId, new SessionSharedPayload(sessionId));
        eventPublisher.publishEvent(new SessionSharedEvent(sessionId, cmd.crewId(), userId, normalized));
        log.info("세션 공유: sessionId={}, crewId={}, tags={}", sessionId, cmd.crewId(), normalized);

        return SharedSessionInfo.from(session, normalized);
    }

    @CacheEvict(value = "sessions", key = "#sessionId", beforeInvocation = true)
    @Transactional
    public void unshareLockedInternal(Long userId, Long sessionId) {
        FocusSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);

        if (!session.isShared()) throw new SessionNotSharedException();

        Long crewId = session.getSharedCrewId();
        session.unshare();
        sessionRepository.save(session);
        // 재공유는 새 태그 입력 흐름이라 철회 시 기존 태그를 함께 제거
        tagRepository.deleteAllBySessionId(sessionId);

        outboxPublisher.publish(OutboxEventType.SESSION_UNSHARED, sessionId, new SessionUnsharedPayload(sessionId));
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

        outboxPublisher.publish(OutboxEventType.SESSION_TAGS_UPDATED, sessionId, new SessionSharedPayload(sessionId));
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
