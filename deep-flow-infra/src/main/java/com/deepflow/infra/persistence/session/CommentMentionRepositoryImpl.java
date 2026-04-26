package com.deepflow.infra.persistence.session;

import com.deepflow.application.common.SliceResult;
import com.deepflow.application.port.out.persistence.CommentMentionRepository;
import com.deepflow.domain.session.comment.CommentMention;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentMentionRepositoryImpl implements CommentMentionRepository {

    private final CommentMentionJpaRepository jpa;

    @Override
    public CommentMention save(CommentMention mention) {
        return jpa.save(mention);
    }

    @Override
    public Optional<CommentMention> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public SliceResult<CommentMention> findUnreadByUserId(Long userId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<CommentMention> slice = (cursorId == null)
                ? jpa.findUnreadByUserId(userId, pageable)
                : jpa.findUnreadByUserIdAfterCursor(userId, cursorId, pageable);
        List<CommentMention> content = slice.getContent();
        Long nextCursor = slice.hasNext() && !content.isEmpty()
                ? content.get(content.size() - 1).getId()
                : null;
        return new SliceResult<>(content, nextCursor, slice.hasNext());
    }

    @Override
    public int markAllReadByUser(Long userId, LocalDateTime readAt) {
        return jpa.markAllReadByUser(userId, readAt);
    }

    @Override
    public List<CommentMention> findByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) return List.of();
        return jpa.findAllByCommentIdIn(commentIds);
    }
}
