package com.deepflow.infra.persistence.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.port.out.persistence.SessionRepository;
import com.deepflow.application.port.out.persistence.SharedFocusSessionSlice;
import com.deepflow.application.session.dto.SharedFeedCursor;
import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionJpaRepository jpaRepository;

    @Override
    public FocusSession save(FocusSession session) {
        return jpaRepository.save(session);
    }

    @Override
    public boolean existsByUserIdAndStatus(Long userId, SessionStatus status) {
        return jpaRepository.existsByUserIdAndStatus(userId, status);
    }

    @Override
    public List<FocusSession> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpaRepository.findAllById(ids);
    }

    @Override
    public Optional<FocusSession> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public SliceResult<FocusSession> findByUserIdWithLog(Long userId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size);

        Slice<FocusSession> slice;
        if (cursorId == null) {
            slice = jpaRepository.findAllByUserIdWithLog(userId, pageable);
        } else {
            slice = jpaRepository.findByUserIdAndIdLessThanWithLog(userId, cursorId, pageable);
        }

        List<FocusSession> content = slice.getContent();
        Long nextCursorId = slice.hasNext()
                ? content.get(content.size() - 1).getId()
                : null;

        return new SliceResult<>(content, nextCursorId, slice.hasNext());
    }

    @Override
    public Optional<FocusSession> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<FocusSession> findByIdAndUserIdWithLogAndImages(Long id, Long userId) {
        return jpaRepository.findByIdAndUserIdWithLogAndImages(id, userId);
    }

    @Override
    public long countByUserIdAndEndTimeHourBetween(Long userId, int fromHour, int toHour) {
        return jpaRepository.countByUserIdAndEndTimeHourBetween(userId, fromHour, toHour);
    }

    @Override
    public long countByUserIdAndDayOfWeek(Long userId, int dayOfWeek) {
        // Java DayOfWeek는 월=1부터, MySQL DAYOFWEEK()는 일=1부터 시작함
        // 변환 없이 넘기면 월요일(Java 1)이 일요일(MySQL 1)로 잘못 매칭됨
        int mysqlDow = (dayOfWeek % 7) + 1;
        return jpaRepository.countByUserIdAndDayOfWeek(userId, mysqlDow);
    }

    @Override
    public long countByUserIdAndDateAndMinDuration(Long userId, LocalDate date, long minDurationSeconds) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        return jpaRepository.countByUserIdAndDateAndMinDuration(userId, dayStart, dayEnd, minDurationSeconds);
    }

    @Override
    public long countByUserIdWithMinContentLength(Long userId, int minLength) {
        return jpaRepository.countByUserIdWithMinContentLength(userId, minLength);
    }

    @Override
    public long countByUserIdWithImages(Long userId) {
        return jpaRepository.countByUserIdWithImages(userId);
    }

    @Override
    public long countTotalImagesByUserId(Long userId) {
        return jpaRepository.countTotalImagesByUserId(userId);
    }

    @Override
    public List<FocusSession> findAllByStatus(SessionStatus status) {
        return jpaRepository.findAllByStatus(status);
    }

    @Override
    public Map<Integer, Long> findHourlyDistribution(Long userId, LocalDateTime from) {
        return jpaRepository.findHourlyDistributionRaw(userId, from).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public long countLogsWithTitle(Long userId) {
        return jpaRepository.countLogsWithTitle(userId);
    }

    @Override
    public double avgContentLength(Long userId) {
        return jpaRepository.avgContentLength(userId);
    }

    @Override
    public List<Long> findOngoingUserIdsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findOngoingUserIdsByUserIds(userIds);
    }

    // --- Crew shared sessions ---

    @Override
    public SharedFocusSessionSlice findSharedByCrewWithCursorFetched(Long crewId, SharedFeedCursor cursor, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<FocusSession> slice = (cursor == null)
                ? jpaRepository.findSharedByCrewWithCursor(crewId, pageable)
                : jpaRepository.findSharedByCrewAfterCursor(crewId, cursor.sharedAt(), cursor.id(), pageable);
        return toSharedFeedSlice(slice);
    }

    @Override
    public SharedFocusSessionSlice findSharedByCrewAndTagWithCursorFetched(Long crewId, String normalizedTag, SharedFeedCursor cursor, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<FocusSession> slice = (cursor == null)
                ? jpaRepository.findSharedByCrewAndTagWithCursor(crewId, normalizedTag, pageable)
                : jpaRepository.findSharedByCrewAndTagAfterCursor(crewId, normalizedTag, cursor.sharedAt(), cursor.id(), pageable);
        return toSharedFeedSlice(slice);
    }

    @Override
    public Optional<FocusSession> findSharedByIdAndCrewWithFetch(Long sessionId, Long crewId) {
        return jpaRepository.findSharedByIdAndCrewWithFetch(sessionId, crewId);
    }

    @Override
    public List<FocusSession> findOngoingSessionsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return jpaRepository.findOngoingSessionsByUserIds(userIds);
    }

    // --- Crew highlight ---

    @Override
    public int countSharedSince(Long crewId, java.time.LocalDateTime since) {
        return jpaRepository.countSharedSince(crewId, since);
    }

    @Override
    public Optional<FocusSession> findHottestSharedSince(Long crewId, java.time.LocalDateTime since) {
        List<FocusSession> result = jpaRepository.findHottestSharedSince(crewId, since, PageRequest.of(0, 1));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<FocusSession> findLongestSharedSince(Long crewId, java.time.LocalDateTime since) {
        List<FocusSession> result = jpaRepository.findLongestSharedSince(crewId, since, PageRequest.of(0, 1));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<FocusSession> findRecentSharedCards(Long crewId, java.time.LocalDateTime since, int limit) {
        return jpaRepository.findRecentSharedCards(crewId, since, PageRequest.of(0, limit));
    }

    private SliceResult<FocusSession> toSliceResult(Slice<FocusSession> slice) {
        List<FocusSession> content = slice.getContent();
        Long nextCursorId = slice.hasNext() && !content.isEmpty()
                ? content.get(content.size() - 1).getId()
                : null;
        return new SliceResult<>(content, nextCursorId, slice.hasNext());
    }

    private SharedFocusSessionSlice toSharedFeedSlice(Slice<FocusSession> slice) {
        List<FocusSession> content = slice.getContent();
        boolean hasNext = slice.hasNext();
        if (!hasNext || content.isEmpty()) {
            return new SharedFocusSessionSlice(content, null, null, hasNext);
        }
        FocusSession last = content.get(content.size() - 1);
        return new SharedFocusSessionSlice(content, last.getSharedAt(), last.getId(), true);
    }
}
