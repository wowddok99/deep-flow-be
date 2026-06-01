package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.tag.SessionTag;

import java.util.List;
import java.util.Map;

public interface SessionTagRepository {

    /**
     * 태그 교체 중 중간 상태가 보이지 않도록 호출자 트랜잭션 안에서 전체 삭제 후 재삽입
     */
    void replaceAll(Long sessionId, List<String> normalizedTags);

    void deleteAllBySessionId(Long sessionId);

    List<SessionTag> findAllBySessionId(Long sessionId);

    /**
     * 피드 목록 조립에서 세션별 태그 조회 N+1 방지
     */
    Map<Long, List<String>> findTagsBySessionIds(List<Long> sessionIds);

    List<TagCount> findPopularTagsByCrewId(Long crewId, int limit);

    List<TagCount> suggestTagsByPrefix(Long crewId, String prefix, int limit);

    List<String> findRecentTagsByUserId(Long userId, int limit);

    record TagCount(String tag, long count) {}
}
