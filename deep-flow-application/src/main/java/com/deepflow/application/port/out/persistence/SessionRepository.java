package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    Map<Integer, Long> findHourlyDistribution(Long userId, LocalDateTime from);

    long countLogsWithTitle(Long userId);

    double avgContentLength(Long userId);

    // --- Crew presence ---
    List<Long> findOngoingUserIdsByUserIds(List<Long> userIds);

    // --- Crew shared sessions ---

    /**
     * 크루 피드 — User + FocusLog fetch join 으로 N+1 방지.
     * 정렬: shared_at DESC, id DESC (커서 결정성 확보).
     */
    SliceResult<FocusSession> findSharedByCrewWithCursorFetched(Long crewId, Long cursorId, int size);

    /**
     * 크루 피드 + 정규화된 태그로 필터.
     */
    SliceResult<FocusSession> findSharedByCrewAndTagWithCursorFetched(Long crewId, String normalizedTag, Long cursorId, int size);

    /**
     * 공유된 세션 상세 (User + FocusLog + Images fetch).
     * 크루 멤버십 체크는 호출자 책임.
     */
    Optional<FocusSession> findSharedByIdAndCrewWithFetch(Long sessionId, Long crewId);

    /**
     * 주어진 사용자 IDs 중 ONGOING 세션을 가진 사용자만 추려 세션 + User fetch.
     * 라이브 프레즌스 스냅샷용.
     */
    List<FocusSession> findOngoingSessionsByUserIds(List<Long> userIds);
}
