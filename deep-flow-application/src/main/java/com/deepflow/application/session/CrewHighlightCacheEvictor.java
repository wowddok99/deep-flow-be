package com.deepflow.application.session;

import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.domain.session.event.SessionReactionAddedEvent;
import com.deepflow.domain.session.event.SessionReactionRemovedEvent;
import com.deepflow.domain.session.event.SessionSharedEvent;
import com.deepflow.domain.session.event.SessionUnsharedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrewHighlightCacheEvictor {

    private static final String CACHE_NAME = "crewHighlight";

    private final CacheManager cacheManager;
    private final SessionRepository sessionRepository;

    // 공유 상태와 리액션 수가 하이라이트 선정 기준이라 관련 변경 커밋 후 캐시 무효화
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShared(SessionSharedEvent e) {
        evict(e.getCrewId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnshared(SessionUnsharedEvent e) {
        evict(e.getCrewId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReactionAdded(SessionReactionAddedEvent e) {
        evictBySessionId(e.getSessionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReactionRemoved(SessionReactionRemovedEvent e) {
        evictBySessionId(e.getSessionId());
    }

    private void evictBySessionId(Long sessionId) {
        sessionRepository.findById(sessionId)
                .map(s -> s.getSharedCrewId())
                .ifPresent(this::evict);
    }

    private void evict(Long crewId) {
        if (crewId == null) return;
        var cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(crewId);
            log.debug("crewHighlight 캐시 evict: crewId={}", crewId);
        }
    }
}
