package com.deepflow.application.session.dto;

import java.util.List;

public record CreateCommentCommand(
        Long parentId,
        String content,
        List<Long> mentions
) {}
