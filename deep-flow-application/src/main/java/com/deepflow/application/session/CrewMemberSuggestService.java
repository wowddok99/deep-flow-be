package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.session.dto.MemberSuggestionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewMemberSuggestService {

    private final CrewMemberRepository crewMemberRepository;

    /**
     * 멘션 자동완성. 본인 제외, prefix 빈 문자열은 빈 리스트.
     * 멤버십 검사 후 username/name prefix 매칭.
     */
    public List<MemberSuggestionInfo> suggestMembers(Long userId, Long crewId, String prefix, int limit) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }
        if (prefix == null || prefix.isBlank()) return List.of();
        return crewMemberRepository.suggestMembers(crewId, userId, prefix.trim(), limit);
    }
}
