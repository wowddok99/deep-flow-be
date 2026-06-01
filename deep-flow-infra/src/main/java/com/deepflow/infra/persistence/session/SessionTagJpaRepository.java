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
     * replaceAll 경로에서는 단일 bulk DELETE 로 교체
     *
     * 직후 saveAll 이 따라오므로 persistence context 정리를 위해 clearAutomatically/flushAutomatically 활성
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SessionTag st WHERE st.sessionId = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);

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
     * 최근 공유 세션 기준 distinct 태그 선정을 위해 서비스에서 중복 제거할 수 있는 순서로 조회
     *
     * sharedAt 동률이면 sessionId desc, tag asc 로 정렬해 결과 순서 고정
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
