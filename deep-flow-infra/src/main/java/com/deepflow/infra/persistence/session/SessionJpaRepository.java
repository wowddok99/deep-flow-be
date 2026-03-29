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
}
