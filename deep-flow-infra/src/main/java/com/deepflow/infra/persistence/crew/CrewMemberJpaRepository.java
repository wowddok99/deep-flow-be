package com.deepflow.infra.persistence.crew;

import com.deepflow.application.crew.dto.MemberSuggestionInfo;
import com.deepflow.domain.crew.CrewMember;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT cm.userId FROM CrewMember cm WHERE cm.crewId = :crewId")
    List<Long> findUserIdsByCrewId(@Param("crewId") Long crewId);

    /**
     * CrewMember 와 User 사이에 JPA 연관관계가 없어 theta-join 사용
     *
     * 호출부에서 이스케이프한 %, _, \\ 값을 리터럴로 처리하기 위해 ESCAPE 절 유지
     */
    @Query("""
            SELECT new com.deepflow.application.crew.dto.MemberSuggestionInfo(u.id, u.name, u.username)
            FROM CrewMember cm, User u
            WHERE cm.userId = u.id
              AND cm.crewId = :crewId
              AND cm.userId <> :excludeUserId
              AND (u.username LIKE :prefix ESCAPE '\\' OR u.name LIKE :prefix ESCAPE '\\')
            ORDER BY u.username ASC
            """)
    List<MemberSuggestionInfo> suggestMembers(
            @Param("crewId") Long crewId,
            @Param("excludeUserId") Long excludeUserId,
            @Param("prefix") String prefixWithWildcard,
            Pageable pageable);
}
