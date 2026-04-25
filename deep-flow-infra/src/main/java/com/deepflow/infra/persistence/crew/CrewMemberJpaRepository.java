package com.deepflow.infra.persistence.crew;

import com.deepflow.domain.crew.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

    Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

    boolean existsByCrewIdAndUserId(Long crewId, Long userId);

    long countByCrewId(Long crewId);

    List<CrewMember> findAllByCrewId(Long crewId);

    @Query("SELECT cm.crewId FROM CrewMember cm WHERE cm.userId = :userId")
    List<Long> findCrewIdsByUserId(@Param("userId") Long userId);

    List<CrewMember> findAllByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM CrewMember cm WHERE cm.crewId = :crewId")
    void deleteAllByCrewId(@Param("crewId") Long crewId);

    @Query("SELECT cm2 FROM CrewMember cm1, CrewMember cm2 " +
            "WHERE cm1.userId = :userId AND cm1.crewId = cm2.crewId")
    List<CrewMember> findAllByCrewIdsSharedWithUser(@Param("userId") Long userId);

    @Query("SELECT cm FROM CrewMember cm WHERE cm.crewId IN :ids")
    List<CrewMember> findAllByCrewIds(@Param("ids") List<Long> ids);
}
