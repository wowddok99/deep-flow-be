package com.deepflow.application.port.out.persistence;

import com.deepflow.application.common.SliceResult;
import com.deepflow.domain.session.comment.CommentMention;

import java.util.List;
import java.util.Optional;

public interface CommentMentionRepository {

    CommentMention save(CommentMention mention);

    Optional<CommentMention> findById(Long id);

    /**
     * 사용자의 미읽음 멘션 목록 (cursor 페이지네이션, id desc).
     */
    SliceResult<CommentMention> findUnreadByUserId(Long userId, Long cursorId, int size);

    /**
     * 사용자 단위 미읽음 일괄 read 처리. 반환: 처리된 row 수.
     */
    int markAllReadByUser(Long userId, java.time.LocalDateTime readAt);

    /**
     * 댓글 batch — 댓글 ID 별 멘션 목록 (응답 조립용 — 현재 미사용, 향후 확장).
     */
    List<CommentMention> findByCommentIds(List<Long> commentIds);
}
