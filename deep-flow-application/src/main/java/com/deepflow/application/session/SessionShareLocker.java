package com.deepflow.application.session;

import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.session.dto.ShareSessionCommand;
import com.deepflow.application.session.dto.SharedSessionInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 세션 공유 토글 임계구역 전용 빈.
 *
 * 같은 클래스 내부 호출은 Spring AOP 프록시를 우회하므로 @DistributedLock 이 적용되지 않는다.
 * CrewJoinLocker 와 동일한 패턴으로 별도 빈에 분리한다.
 *
 * 진입 시점 외부 트랜잭션이 없어야 락 → REQUIRES_NEW TX 시작/커밋 → 락 해제 순서가 보장되므로
 * 이 메서드 자체에는 @Transactional 을 두지 않는다.
 */
@Component
public class SessionShareLocker {

    private final SessionShareService service;

    public SessionShareLocker(@Lazy SessionShareService service) {
        this.service = service;
    }

    @DistributedLock(key = "'session_share:' + #sessionId")
    public SharedSessionInfo share(Long userId, Long sessionId, ShareSessionCommand cmd) {
        return service.shareLockedInternal(userId, sessionId, cmd);
    }

    @DistributedLock(key = "'session_share:' + #sessionId")
    public void unshare(Long userId, Long sessionId) {
        service.unshareLockedInternal(userId, sessionId);
    }

    @DistributedLock(key = "'session_share:' + #sessionId")
    public SharedSessionInfo updateTags(Long userId, Long sessionId, List<String> tags) {
        return service.updateTagsLockedInternal(userId, sessionId, tags);
    }
}
