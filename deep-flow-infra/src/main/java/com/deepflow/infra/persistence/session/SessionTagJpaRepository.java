package com.deepflow.infra.persistence.session;

import com.deepflow.domain.session.tag.SessionTag;
import com.deepflow.domain.session.tag.SessionTagId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface SessionTagJpaRepository extends JpaRepository<SessionTag, SessionTagId> {

    @Query("SELECT st FROM SessionTag st WHERE st.sessionId IN :sessionIds")
    List<SessionTag> findAllBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    List<SessionTag> findAllBySessionId(Long sessionId);

    /**
     * Spring Data 의 derived 'deleteAllBy...' 는 SELECT + 단건 DELETE × N 을 발생시키므로
     * 명시적으로 단일 bulk DELETE 로 교체. replaceAll 직후 saveAll 이 따라오니
     * persistence context 정리를 위해 clearAutomatically/flushAutomatically 모두 활성.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SessionTag st WHERE st.sessionId = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 크루의 인기 태그 빈도 집계.
     * shared_crew_id 가 동일하고 deleted_at IS NULL 인 세션의 태그만 카운트.
     */
    @Query("""
            SELECT st.tag AS tag, COUNT(st) AS cnt
            FROM SessionTag st
            JOIN FocusSession fs ON fs.id = st.sessionId
            WHERE fs.sharedCrewId = :crewId
              AND fs.deletedAt IS NULL
            GROUP BY st.tag
            ORDER BY cnt DESC, st.tag ASC
            """)
    List<Object[]> findPopularTagsByCrewId(@Param("crewId") Long crewId, Pageable pageable);

    @Query("""
            SELECT st.tag AS tag, COUNT(st) AS cnt
            FROM SessionTag st
            JOIN FocusSession fs ON fs.id = st.sessionId
            WHERE fs.sharedCrewId = :crewId
              AND fs.deletedAt IS NULL
              AND st.tag LIKE :prefix
            GROUP BY st.tag
            ORDER BY cnt DESC, st.tag ASC
            """)
    List<Object[]> suggestTagsByPrefix(
            @Param("crewId") Long crewId,
            @Param("prefix") String prefixWithWildcard,
            Pageable pageable);

    /**
     * 사용자가 가장 최근에 공유한 세션들의 태그를 distinct 한 채로 최신순 반환.
     * 최신 공유 세션을 먼저 보고, 같은 태그가 이미 있으면 스킵하는 로직은 서비스에서 처리.
     * tie-breaker: sharedAt 동률이면 sessionId desc → tag asc (composite PK 순서 보존).
     */
    @Query("""
            SELECT st.tag, fs.sharedAt
            FROM SessionTag st
            JOIN FocusSession fs ON fs.id = st.sessionId
            WHERE fs.user.id = :userId
              AND fs.sharedAt IS NOT NULL
              AND fs.deletedAt IS NULL
            ORDER BY fs.sharedAt DESC, st.sessionId DESC, st.tag ASC
            """)
    List<Object[]> findRecentTagsByUserId(@Param("userId") Long userId, Pageable pageable);
}
