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
}
