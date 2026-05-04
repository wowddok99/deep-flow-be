package com.deepflow.infra.persistence.session;

import com.deepflow.application.port.out.persistence.SessionTagRepository;
import com.deepflow.domain.session.tag.SessionTag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SessionTagRepositoryImpl implements SessionTagRepository {

    private final SessionTagJpaRepository jpa;

    @Override
    @Transactional
    public void replaceAll(Long sessionId, List<String> normalizedTags) {
        jpa.deleteAllBySessionId(sessionId);
        if (normalizedTags == null || normalizedTags.isEmpty()) return;

        // 유효하지 않은 태그(Null, 빈 문자열, 중복)가 DB에 적재되지 않도록 한 번 더 필터링
        List<SessionTag> entities = normalizedTags.stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .map(t -> SessionTag.of(sessionId, t))
                .toList();
        jpa.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteAllBySessionId(Long sessionId) {
        jpa.deleteAllBySessionId(sessionId);
    }

    @Override
    public List<SessionTag> findAllBySessionId(Long sessionId) {
        return jpa.findAllBySessionId(sessionId);
    }

    @Override
    public Map<Long, List<String>> findTagsBySessionIds(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return Map.of();
        return jpa.findAllBySessionIds(sessionIds).stream()
                .collect(Collectors.groupingBy(
                        SessionTag::getSessionId,
                        Collectors.mapping(SessionTag::getTag, Collectors.toList())
                ));
    }

    @Override
    public List<TagCount> findPopularTagsByCrewId(Long crewId, int limit) {
        return jpa.findPopularTagsByCrewId(crewId, PageRequest.of(0, limit)).stream()
                .map(row -> new TagCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    public List<TagCount> suggestTagsByPrefix(Long crewId, String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) return List.of();
        // 사용자가 입력한 '%', '_' 문자가 검색 와일드카드로 오동작하지 않도록 이스케이프 처리
        String escaped = prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return jpa.suggestTagsByPrefix(crewId, escaped + "%", PageRequest.of(0, limit)).stream()
                .map(row -> new TagCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    public List<String> findRecentTagsByUserId(Long userId, int limit) {
        // DB 레벨의 중복 제거(distinct)와 정렬을 함께 쓰면 발생하는 성능/정확도 이슈를 피하기 위해 메모리에서 중복을 제거
        // 세션당 평균 5개의 태그를 쓴다고 가정하여 여유 있게 조회
        int fetchSize = Math.max(limit * 5, limit);
        List<Object[]> rows = jpa.findRecentTagsByUserId(userId, PageRequest.of(0, fetchSize));

        LinkedHashSet<String> dedup = new LinkedHashSet<>();
        for (Object[] row : rows) {
            String tag = (String) row[0];
            dedup.add(tag);
            if (dedup.size() >= limit) break;
        }
        return List.copyOf(dedup);
    }
}
