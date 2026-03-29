package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {

    FocusSession save(FocusSession session);

    Optional<FocusSession> findById(Long id);

    boolean existsByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<FocusSession> findByIdAndUserId(Long id, Long userId);

    SliceResult<FocusSession> findByUserIdWithLog(Long userId, Long cursorId, int size);

    Optional<FocusSession> findByIdAndUserIdWithLogAndImages(Long id, Long userId);

    long countByUserIdAndEndTimeHourBetween(Long userId, int fromHour, int toHour);

    long countByUserIdAndDayOfWeek(Long userId, int dayOfWeek);

    long countByUserIdAndDateAndMinDuration(Long userId, LocalDate date, long minDurationSeconds);

    long countByUserIdWithMinContentLength(Long userId, int minLength);

    long countByUserIdWithImages(Long userId);

    long countTotalImagesByUserId(Long userId);

    List<FocusSession> findAllByStatus(SessionStatus status);
}
