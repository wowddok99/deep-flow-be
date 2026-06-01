package com.deepflow.infra.persistence.crew;

import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.session.dto.MemberSuggestionInfo;
import com.deepflow.domain.crew.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public List<CrewMember> findAllByCrewIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpaRepository.findAllByCrewIds(ids);
    }

    @Override
    public java.util.Set<Long> findUserIdsByCrewId(Long crewId) {
        return new java.util.HashSet<>(jpaRepository.findUserIdsByCrewId(crewId));
    }

    @Override
    public List<MemberSuggestionInfo> suggestMembers(Long crewId, Long excludeUserId, String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) return List.of();
        // 사용자 입력 %, _, \\ 문자가 LIKE 와일드카드로 동작하지 않도록 이스케이프
        String escaped = prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return jpaRepository.suggestMembers(crewId, excludeUserId, escaped + "%", PageRequest.of(0, limit));
    }
}
