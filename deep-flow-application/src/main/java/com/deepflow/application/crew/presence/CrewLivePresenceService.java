package com.deepflow.application.crew.presence;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.crew.dto.LivePresenceInfo;
import com.deepflow.domain.crew.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 크루 페이지 진입 시 현재 집중 중인 멤버 스냅샷 조회
 *
 * 이후 변동 사항은 SSE 스트림에서 실시간 푸시
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewLivePresenceService {

    private final CrewMemberRepository crewMemberRepository;
    private final SessionRepository sessionRepository;

    public LivePresenceInfo getLivePresence(Long userId, Long crewId) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }

        List<Long> memberIds = crewMemberRepository.findAllByCrewId(crewId).stream()
                .map(CrewMember::getUserId)
                .toList();
        if (memberIds.isEmpty()) {
            return new LivePresenceInfo(List.of());
        }

        List<LivePresenceInfo.ActiveMember> active = sessionRepository.findOngoingSessionsByUserIds(memberIds).stream()
                .map(s -> new LivePresenceInfo.ActiveMember(
                        s.getUser() != null ? s.getUser().getId() : null,
                        s.getUser() != null ? s.getUser().getName() : "알수없음",
                        s.getStartTime()
                ))
                .sorted(Comparator.comparing(LivePresenceInfo.ActiveMember::sessionStartedAt))
                .toList();

        return new LivePresenceInfo(active);
    }
}
