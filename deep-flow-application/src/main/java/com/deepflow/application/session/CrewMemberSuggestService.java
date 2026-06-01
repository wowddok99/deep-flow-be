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
     * 크루 댓글 멘션 자동완성을 위해 본인을 제외한 멤버를 입력 접두어로 검색
     */
    public List<MemberSuggestionInfo> suggestMembers(Long userId, Long crewId, String prefix, int limit) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }
        if (prefix == null || prefix.isBlank()) return List.of();
        return crewMemberRepository.suggestMembers(crewId, userId, prefix.trim(), limit);
    }
}
