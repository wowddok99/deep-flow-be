package com.deepflow.infra.persistence.session;

import com.deepflow.domain.session.comment.CommentMention;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

interface CommentMentionJpaRepository extends JpaRepository<CommentMention, Long> {

    @Query("""
            SELECT m FROM CommentMention m
            WHERE m.userId = :userId
              AND m.readAt IS NULL
            ORDER BY m.id DESC
            """)
    Slice<CommentMention> findUnreadByUserId(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("""
            SELECT m FROM CommentMention m
            WHERE m.userId = :userId
              AND m.readAt IS NULL
              AND m.id < :cursorId
            ORDER BY m.id DESC
            """)
    Slice<CommentMention> findUnreadByUserIdAfterCursor(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CommentMention m
            SET m.readAt = :readAt
            WHERE m.userId = :userId
              AND m.readAt IS NULL
            """)
    int markAllReadByUser(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    List<CommentMention> findAllByCommentIdIn(List<Long> commentIds);
}
