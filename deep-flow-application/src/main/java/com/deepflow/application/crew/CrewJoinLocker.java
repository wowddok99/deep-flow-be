package com.deepflow.application.crew;

import com.deepflow.application.crew.dto.CrewSummaryInfo;
import com.deepflow.application.lock.DistributedLock;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 크루 가입 임계구역 전용 빈
 *
 * CrewService 내부 호출은 Spring AOP 프록시를 거치지 않아 분산 락이 적용되지 않으므로 별도 빈으로 분리
 * 외부 트랜잭션이 먼저 열리면 락 해제 전에 커밋이 끝나지 않을 수 있어 이 빈의 진입점에는 @Transactional 을 두지 않음
 *
 * CrewService 와 양방향 의존이라 @Lazy 로 순환을 끊음
 */
@Component
public class CrewJoinLocker {

    private final CrewService crewService;

    public CrewJoinLocker(@Lazy CrewService crewService) {
        this.crewService = crewService;
    }

    @DistributedLock(key = "'crew_join:' + #crewId")
    public CrewSummaryInfo join(Long userId, Long crewId, boolean requirePublic) {
        return crewService.joinCrewLockedInternal(userId, crewId, requirePublic);
    }
}
