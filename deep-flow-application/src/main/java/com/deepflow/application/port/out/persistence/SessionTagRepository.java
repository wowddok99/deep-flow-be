package com.deepflow.application.port.out.persistence;

import com.deepflow.domain.session.tag.SessionTag;

import java.util.List;
import java.util.Map;

public interface SessionTagRepository {

    /**
     * 세션의 기존 태그 전부 삭제 후 정규화된 태그 리스트로 교체.
     * 호출자(서비스)가 같은 트랜잭션 안에서 호출해야 함.
     */
    void replaceAll(Long sessionId, List<String> normalizedTags);

    void deleteAllBySessionId(Long sessionId);

    List<SessionTag> findAllBySessionId(Long sessionId);

    /**
     * 여러 세션의 태그를 한 번에 조회 후 sessionId 별로 묶음.
     * 피드 N+1 방지용.
     */
    Map<Long, List<String>> findTagsBySessionIds(List<Long> sessionIds);

    /**
     * 크루의 인기 태그 Top N (빈도 내림차순).
     * 반환: List<TagCount>
     */
    List<TagCount> findPopularTagsByCrewId(Long crewId, int limit);

    /**
     * 크루 안에서 prefix 매칭되는 태그 Top N (빈도 내림차순).
     * 자동완성용.
     */
    List<TagCount> suggestTagsByPrefix(Long crewId, String prefix, int limit);

    /**
     * 사용자가 최근 사용한 distinct 태그 Top N (최근 공유 시각 내림차순).
     */
    List<String> findRecentTagsByUserId(Long userId, int limit);

    record TagCount(String tag, long count) {}
}
