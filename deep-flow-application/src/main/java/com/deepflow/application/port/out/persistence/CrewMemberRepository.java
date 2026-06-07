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
     * 멘션과 알림 가드에서 멤버십을 빠르게 확인하기 위한 크루 사용자 ID 집합
     */
    java.util.Set<Long> findUserIdsByCrewId(Long crewId);

    /**
     * 댓글 멘션 자동완성에서 본인을 제외하고 사용자명과 이름을 입력 접두어로 매칭
     */
    List<com.deepflow.application.crew.dto.MemberSuggestionInfo> suggestMembers(
            Long crewId, Long excludeUserId, String prefix, int limit);
}
