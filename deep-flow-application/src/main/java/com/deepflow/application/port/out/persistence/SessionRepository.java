package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.session.dto.SharedFeedCursor;
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

    /**
     * 세션 ID 목록으로 batch 조회. 알림 deep link 매핑 등에서 sharedCrewId 만 빠르게 가져올 때 사용.
     * fetch join 없음 — sharedCrewId/sharedAt 만 필요한 경우 가벼운 호출.
     */
    List<FocusSession> findAllByIds(List<Long> ids);

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
     * 정렬: shared_at DESC, id DESC. WHERE 조건도 동일 키 (keyset pagination) 사용.
     * cursor == null 이면 첫 페이지.
     */
    SharedFocusSessionSlice findSharedByCrewWithCursorFetched(Long crewId, SharedFeedCursor cursor, int size);

    /**
     * 크루 피드 + 정규화된 태그 필터. cursor == null 이면 첫 페이지.
     */
    SharedFocusSessionSlice findSharedByCrewAndTagWithCursorFetched(Long crewId, String normalizedTag, SharedFeedCursor cursor, int size);

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

    // --- Crew highlight ---

    /**
     * 크루의 최근 since 이후 공유 세션 카운트. 하이라이트 모드 결정용.
     */
    int countSharedSince(Long crewId, LocalDateTime since);

    /**
     * MATURE 모드 — 점수 = reactionCount / hours_since_post 가 가장 높은 세션 1개.
     * since 이후 공유된 세션 중에서.
     */
    Optional<FocusSession> findHottestSharedSince(Long crewId, LocalDateTime since);

    /**
     * MATURE 모드 — durationSeconds 가 가장 큰 세션 1개. since 이후 공유.
     */
    Optional<FocusSession> findLongestSharedSince(Long crewId, LocalDateTime since);

    /**
     * GROWING 모드 — 최신 공유 세션 카드 N 개.
     */
    List<FocusSession> findRecentSharedCards(Long crewId, LocalDateTime since, int limit);
}
