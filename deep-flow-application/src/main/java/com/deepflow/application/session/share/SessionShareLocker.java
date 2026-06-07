package com.deepflow.application.session.share;

import com.deepflow.application.lock.DistributedLock;
import com.deepflow.application.session.share.dto.ShareSessionCommand;
import com.deepflow.application.session.share.dto.SharedSessionInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 세션 공유 변경 임계구역 전용 빈
 *
 * SessionShareService 내부 호출은 Spring AOP 프록시를 거치지 않아 분산 락이 적용되지 않으므로 별도 빈으로 분리
 * 외부 트랜잭션이 먼저 열리면 락 해제 전에 커밋이 끝나지 않을 수 있어 이 빈의 진입점에는 @Transactional 을 두지 않음
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
