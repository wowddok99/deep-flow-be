package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.crew.dto.SharedFeedCursor;
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
     * 알림 이동 경로 매핑처럼 sharedCrewId, sharedAt 만 필요한 경로에서 연관 데이터를 로드하지 않고 한 번에 조회
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

    List<Long> findOngoingUserIdsByUserIds(List<Long> userIds);

    /**
     * 크루 피드 조회에서 User, FocusLog 를 함께 로드해 목록 조립 시 N+1 방지
     *
     * 정렬 키는 sharedAt DESC, id DESC 이며 cursor 도 같은 키 사용
     */
    SharedFocusSessionSlice findSharedByCrewWithCursorFetched(Long crewId, SharedFeedCursor cursor, int size);

    /**
     * 정규화된 태그로 필터링하면서 크루 피드와 같은 커서 정렬 기준 사용
     */
    SharedFocusSessionSlice findSharedByCrewAndTagWithCursorFetched(Long crewId, String normalizedTag, SharedFeedCursor cursor, int size);

    /**
     * 공유 세션 상세에서 본문과 이미지를 한 번에 노출하기 위해 작성자, 집중 기록, 이미지를 함께 조회
     *
     * 크루 멤버십 체크는 호출자 책임
     */
    Optional<FocusSession> findSharedByIdAndCrewWithFetch(Long sessionId, Long crewId);

    /**
     * 라이브 프레즌스 스냅샷 조립을 위해 진행 중 세션과 User 를 함께 조회
     */
    List<FocusSession> findOngoingSessionsByUserIds(List<Long> userIds);

    int countSharedSince(Long crewId, LocalDateTime since);

    /**
     * 하이라이트 MATURE 모드의 뜨거운 글 카드 선정을 위해 시간 대비 리액션 점수 기준 조회
     */
    Optional<FocusSession> findHottestSharedSince(Long crewId, LocalDateTime since);

    Optional<FocusSession> findLongestSharedSince(Long crewId, LocalDateTime since);

    List<FocusSession> findRecentSharedCards(Long crewId, LocalDateTime since, int limit);
}
