package com.deepflow.core.repository.session;

import com.deepflow.core.domain.session.FocusSession;
import com.deepflow.core.domain.session.SessionStatus;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    // User-scoped checks
    boolean existsByUserIdAndStatus(Long userId, SessionStatus status);
    Optional<FocusSession> findByIdAndUserId(Long id, Long userId);

    // 커서 페이지네이션 - N+1 방지를 위해 focusLog fetch join
    // 첫 페이지 조회
    @Query("SELECT s FROM FocusSession s JOIN FETCH s.focusLog WHERE s.user.id = :userId ORDER BY s.id DESC")
    Slice<FocusSession> findAllByUserIdWithLog(@Param("userId") Long userId, Pageable pageable);

    // 커서 이후 페이지 조회 - id < cursorId 조건으로 다음 데이터 탐색
    @Query("SELECT s FROM FocusSession s JOIN FETCH s.focusLog WHERE s.user.id = :userId AND s.id < :cursorId ORDER BY s.id DESC")
    Slice<FocusSession> findByUserIdAndIdLessThanWithLog(@Param("userId") Long userId, @Param("cursorId") Long cursorId, Pageable pageable);

    @Query("SELECT s FROM FocusSession s JOIN FETCH s.focusLog fl LEFT JOIN FETCH fl.images WHERE s.id = :id AND s.user.id = :userId")
    Optional<FocusSession> findByIdAndUserIdWithLogAndImages(@Param("id") Long id, @Param("userId") Long userId);
}
