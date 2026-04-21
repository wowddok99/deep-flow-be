package com.deepflow.infra.persistence.crew;

import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.domain.crew.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CrewMemberRepositoryImpl implements CrewMemberRepository {

    private final CrewMemberJpaRepository jpaRepository;

    @Override
    public CrewMember save(CrewMember member) {
        return jpaRepository.save(member);
    }

    @Override
    public void delete(CrewMember member) {
        jpaRepository.delete(member);
    }

    @Override
    public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
        return jpaRepository.findByCrewIdAndUserId(crewId, userId);
    }

    @Override
    public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
        return jpaRepository.existsByCrewIdAndUserId(crewId, userId);
    }

    @Override
    public long countByCrewId(Long crewId) {
        return jpaRepository.countByCrewId(crewId);
    }

    @Override
    public List<CrewMember> findAllByCrewId(Long crewId) {
        return jpaRepository.findAllByCrewId(crewId);
    }

    @Override
    public List<Long> findCrewIdsByUserId(Long userId) {
        return jpaRepository.findCrewIdsByUserId(userId);
    }

    @Override
    public List<CrewMember> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId);
    }

    @Override
    public void deleteAllByCrewId(Long crewId) {
        jpaRepository.deleteAllByCrewId(crewId);
    }

    @Override
    public List<CrewMember> findAllByCrewIdsSharedWithUser(Long userId) {
        return jpaRepository.findAllByCrewIdsSharedWithUser(userId);
    }
}
