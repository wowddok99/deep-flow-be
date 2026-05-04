package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.crew.CrewMember;

import java.util.List;
import java.util.Optional;

public interface CrewMemberRepository {

    CrewMember save(CrewMember member);

    void delete(CrewMember member);

    Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

    boolean existsByCrewIdAndUserId(Long crewId, Long userId);

    long countByCrewId(Long crewId);

    List<CrewMember> findAllByCrewId(Long crewId);

    List<Long> findCrewIdsByUserId(Long userId);

    List<CrewMember> findAllByUserId(Long userId);

    void deleteAllByCrewId(Long crewId);

    List<CrewMember> findAllByCrewIdsSharedWithUser(Long userId);

    List<CrewMember> findAllByCrewIds(List<Long> ids);

    /**
     * 크루의 user_id Set — 멘션/알림 가드용 (멤버십 체크 빈도 높은 경로에서 batch 사용).
     */
    java.util.Set<Long> findUserIdsByCrewId(Long crewId);

    /**
     * 크루 멤버 중 username/name prefix 매칭. 본인은 제외.
     * 멘션 자동완성용. 결과는 username 사전순 정렬.
     */
    List<com.deepflow.application.session.dto.MemberSuggestionInfo> suggestMembers(
            Long crewId, Long excludeUserId, String prefix, int limit);
}
