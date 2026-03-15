package com.deepflow.infra.persistence.session;

import com.deepflow.domain.session.FocusSession;
import com.deepflow.domain.session.SessionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
