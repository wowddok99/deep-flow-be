package com.deepflow.application.crew;

import com.deepflow.application.crew.dto.CrewPresencePayload;
import com.deepflow.application.port.out.notification.CrewPresenceNotifier;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.UserRepository;
import com.deepflow.domain.crew.CrewMember;
import com.deepflow.domain.session.event.SessionStartedEvent;
import com.deepflow.domain.session.event.SessionStoppedEvent;
import com.deepflow.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrewPresenceService {

    private final CrewMemberRepository crewMemberRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final CrewPresenceNotifier notifier;

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionStarted(SessionStartedEvent event) {
        broadcastPresence(event.getUserId(), true);
    }

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionStopped(SessionStoppedEvent event) {
        broadcastPresence(event.getUserId(), false);
    }

    private void broadcastPresence(Long userId, boolean isActive) {
        try {
            List<CrewMember> sharedMemberships = crewMemberRepository.findAllByCrewIdsSharedWithUser(userId);
            if (sharedMemberships.isEmpty()) return;

            String userName = userRepository.findById(userId)
                    .map(User::getName).orElse("");

            Map<Long, List<Long>> membersByCrew = sharedMemberships.stream()
                    .collect(Collectors.groupingBy(
                            CrewMember::getCrewId,
                            Collectors.mapping(CrewMember::getUserId, Collectors.toList())));

            for (Map.Entry<Long, List<Long>> entry : membersByCrew.entrySet()) {
                Long crewId = entry.getKey();
                List<Long> allMemberIds = entry.getValue();
                long activeCount = sessionRepository.findOngoingUserIdsByUserIds(allMemberIds).size();

                CrewPresencePayload payload = new CrewPresencePayload(
                        crewId, userId, userName, isActive, activeCount
                );
                notifier.broadcastToUsers(allMemberIds, payload);
            }
        } catch (Exception e) {
            log.error("Crew presence broadcast failed: userId={}", userId, e);
        }
    }
}
