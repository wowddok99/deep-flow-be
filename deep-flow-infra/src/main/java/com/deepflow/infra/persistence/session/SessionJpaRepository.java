package com.deepflow.infra.persistence.session;

import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface SessionJpaRepository extends JpaRepository<FocusSession, Long> {

    boolean existsByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<FocusSession> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT s FROM FocusSession s JOIN FETCH s.focusLog WHERE s.user.id = :userId ORDER BY s.id DESC")
    Slice<FocusSession> findAllByUserIdWithLog(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM FocusSession s JOIN FETCH s.focusLog WHERE s.user.id = :userId AND s.id < :cursorId ORDER BY s.id DESC")
    Slice<FocusSession> findByUserIdAndIdLessThanWithLog(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("SELECT s FROM FocusSession s JOIN FETCH s.focusLog fl LEFT JOIN FETCH fl.images WHERE s.id = :id AND s.user.id = :userId")
    Optional<FocusSession> findByIdAndUserIdWithLogAndImages(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND HOUR(s.endTime) >= :fromHour AND HOUR(s.endTime) < :toHour")
    long countByUserIdAndEndTimeHourBetween(@Param("userId") Long userId, @Param("fromHour") int fromHour, @Param("toHour") int toHour);

    @Query("SELECT COUNT(s) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND DAYOFWEEK(s.startTime) = :dow")
    long countByUserIdAndDayOfWeek(@Param("userId") Long userId, @Param("dow") int dow);

    @Query("SELECT COUNT(s) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND s.startTime >= :dayStart AND s.startTime < :dayEnd AND s.durationSeconds >= :minDuration")
    long countByUserIdAndDateAndMinDuration(@Param("userId") Long userId, @Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd, @Param("minDuration") long minDuration);

    @Query("SELECT COUNT(s) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND LENGTH(s.focusLog.content) >= :minLength")
    long countByUserIdWithMinContentLength(@Param("userId") Long userId, @Param("minLength") int minLength);

    @Query("SELECT COUNT(s) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND SIZE(s.focusLog.images) > 0")
    long countByUserIdWithImages(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(SIZE(s.focusLog.images)), 0) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED'")
    long countTotalImagesByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM FocusSession s JOIN FETCH s.user JOIN FETCH s.focusLog WHERE s.status = :status AND s.deletedAt IS NULL")
    List<FocusSession> findAllByStatus(@Param("status") SessionStatus status);

    @Query(value = """
            WITH RECURSIVE hours(h) AS (
                SELECT 0 UNION ALL SELECT h+1 FROM hours WHERE h < 23
            )
            SELECT hours.h, COUNT(s.id)
            FROM hours
            LEFT JOIN focus_session s
              ON s.user_id = :uid
             AND s.status = 'COMPLETED'
             AND s.deleted_at IS NULL
             AND s.start_time >= :from
             AND (
                   (DATE(s.start_time) = DATE(s.end_time)
                    AND HOUR(s.start_time) <= hours.h
                    AND HOUR(s.end_time)   >= hours.h)
                OR (DATE(s.start_time) <> DATE(s.end_time)
                    AND (hours.h >= HOUR(s.start_time)
                         OR hours.h <= HOUR(s.end_time)))
                 )
            GROUP BY hours.h
            """, nativeQuery = true)
    List<Object[]> findHourlyDistributionRaw(
            @Param("uid") Long userId,
            @Param("from") LocalDateTime from);

    @Query("SELECT COUNT(s) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND s.focusLog.title IS NOT NULL AND s.focusLog.title <> ''")
    long countLogsWithTitle(@Param("userId") Long userId);

    @Query("SELECT COALESCE(AVG(LENGTH(s.focusLog.content)), 0) FROM FocusSession s WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND s.focusLog.content IS NOT NULL AND s.focusLog.content <> '{}'")
    double avgContentLength(@Param("userId") Long userId);

    @Query("SELECT DISTINCT s.user.id FROM FocusSession s WHERE s.user.id IN :userIds AND s.status = 'ONGOING'")
    List<Long> findOngoingUserIdsByUserIds(@Param("userIds") List<Long> userIds);

    // --- Crew shared sessions ---

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            WHERE s.sharedCrewId = :crewId
            ORDER BY s.sharedAt DESC, s.id DESC
            """)
    Slice<FocusSession> findSharedByCrewWithCursor(
            @Param("crewId") Long crewId,
            Pageable pageable);

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            WHERE s.sharedCrewId = :crewId
              AND (s.sharedAt < :cursorSharedAt
                   OR (s.sharedAt = :cursorSharedAt AND s.id < :cursorId))
            ORDER BY s.sharedAt DESC, s.id DESC
            """)
    Slice<FocusSession> findSharedByCrewAfterCursor(
            @Param("crewId") Long crewId,
            @Param("cursorSharedAt") LocalDateTime cursorSharedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            JOIN com.deepflow.domain.session.tag.SessionTag st ON st.sessionId = s.id
            WHERE s.sharedCrewId = :crewId
              AND st.tag = :tag
            ORDER BY s.sharedAt DESC, s.id DESC
            """)
    Slice<FocusSession> findSharedByCrewAndTagWithCursor(
            @Param("crewId") Long crewId,
            @Param("tag") String tag,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            JOIN com.deepflow.domain.session.tag.SessionTag st ON st.sessionId = s.id
            WHERE s.sharedCrewId = :crewId
              AND st.tag = :tag
              AND (s.sharedAt < :cursorSharedAt
                   OR (s.sharedAt = :cursorSharedAt AND s.id < :cursorId))
            ORDER BY s.sharedAt DESC, s.id DESC
            """)
    Slice<FocusSession> findSharedByCrewAndTagAfterCursor(
            @Param("crewId") Long crewId,
            @Param("tag") String tag,
            @Param("cursorSharedAt") LocalDateTime cursorSharedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog fl
            LEFT JOIN FETCH fl.images
            WHERE s.id = :sessionId
              AND s.sharedCrewId = :crewId
            """)
    Optional<FocusSession> findSharedByIdAndCrewWithFetch(
            @Param("sessionId") Long sessionId,
            @Param("crewId") Long crewId);

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            WHERE s.user.id IN :userIds
              AND s.status = 'ONGOING'
            """)
    List<FocusSession> findOngoingSessionsByUserIds(@Param("userIds") List<Long> userIds);

    // --- Crew highlight ---

    @Query("""
            SELECT COUNT(s) FROM FocusSession s
            WHERE s.sharedCrewId = :crewId
              AND s.deletedAt IS NULL
              AND s.sharedAt >= :since
            """)
    int countSharedSince(@Param("crewId") Long crewId, @Param("since") LocalDateTime since);

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            LEFT JOIN com.deepflow.domain.session.reaction.SessionReaction sr ON sr.sessionId = s.id
            WHERE s.sharedCrewId = :crewId
              AND s.deletedAt IS NULL
              AND s.sharedAt >= :since
            GROUP BY s
            ORDER BY (CAST(COUNT(sr) AS double) /
                     GREATEST(CAST(TIMESTAMPDIFF(HOUR, s.sharedAt, CURRENT_TIMESTAMP) AS double), 1.0)) DESC,
                     s.sharedAt DESC
            """)
    List<FocusSession> findHottestSharedSince(
            @Param("crewId") Long crewId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            WHERE s.sharedCrewId = :crewId
              AND s.deletedAt IS NULL
              AND s.sharedAt >= :since
            ORDER BY s.durationSeconds DESC, s.sharedAt DESC
            """)
    List<FocusSession> findLongestSharedSince(
            @Param("crewId") Long crewId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    @Query("""
            SELECT s FROM FocusSession s
            JOIN FETCH s.user
            JOIN FETCH s.focusLog
            WHERE s.sharedCrewId = :crewId
              AND s.deletedAt IS NULL
              AND s.sharedAt >= :since
            ORDER BY s.sharedAt DESC, s.id DESC
            """)
    List<FocusSession> findRecentSharedCards(
            @Param("crewId") Long crewId,
            @Param("since") LocalDateTime since,
            Pageable pageable);
}
