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

        // API 밖에서 호출돼도 잘못된 태그가 저장되지 않도록 저장 직전 재검증
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
        // 사용자 입력 %, _ 문자가 LIKE 와일드카드로 동작하지 않도록 이스케이프
        String escaped = prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return jpa.suggestTagsByPrefix(crewId, escaped + "%", PageRequest.of(0, limit)).stream()
                .map(row -> new TagCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    public List<String> findRecentTagsByUserId(Long userId, int limit) {
        // 최신순 정렬을 보존하기 위해 DB distinct 대신 메모리에서 중복 제거
        // 세션당 평균 태그 수를 기준으로 limit 보다 여유 있게 조회
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
