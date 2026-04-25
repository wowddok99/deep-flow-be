package com.deepflow.application.session;

import com.deepflow.application.exception.crew.NotCrewMemberException;
import com.deepflow.application.port.out.persistence.CrewMemberRepository;
import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.application.session.dto.TagSuggestInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionTagService {

    private final SessionTagRepository tagRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final TagNormalizer tagNormalizer;

    public List<TagSuggestInfo> getPopularTags(Long userId, Long crewId, int limit) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }
        return tagRepository.findPopularTagsByCrewId(crewId, limit).stream()
                .map(tc -> new TagSuggestInfo(tc.tag(), tc.count()))
                .toList();
    }

    public List<TagSuggestInfo> suggestTags(Long userId, Long crewId, String prefix, int limit) {
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new NotCrewMemberException();
        }
        if (prefix == null || prefix.isBlank()) return List.of();

        // 사용자가 'JPA' 입력해도 'jpa' prefix 로 매칭되도록 정규화
        String normalized = tagNormalizer.normalize(prefix);
        if (normalized.isBlank()) return List.of();

        return tagRepository.suggestTagsByPrefix(crewId, normalized, limit).stream()
                .map(tc -> new TagSuggestInfo(tc.tag(), tc.count()))
                .toList();
    }

    public List<String> getMyRecentTags(Long userId, int limit) {
        return tagRepository.findRecentTagsByUserId(userId, limit);
    }
}
