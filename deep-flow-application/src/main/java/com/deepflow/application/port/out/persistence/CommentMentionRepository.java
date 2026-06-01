package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.domain.session.comment.CommentMention;

import java.util.List;
import java.util.Optional;

public interface CommentMentionRepository {

    CommentMention save(CommentMention mention);

    Optional<CommentMention> findById(Long id);

    SliceResult<CommentMention> findUnreadByUserId(Long userId, Long cursorId, int size);

    int markAllReadByUser(Long userId, java.time.LocalDateTime readAt);

    /**
     * 댓글 목록 응답 조립에서 멘션 조회 N+1 방지
     */
    List<CommentMention> findByCommentIds(List<Long> commentIds);
}
