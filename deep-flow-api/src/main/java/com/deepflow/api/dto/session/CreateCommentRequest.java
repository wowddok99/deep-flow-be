package com.deepflow.api.dto.session;

import com.deepflow.application.session.dto.CreateCommentCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCommentRequest(
        Long parentId,
        @NotBlank @Size(max = 2000) String content,
        List<Long> mentions
) {
    public CreateCommentCommand toCommand() {
        return new CreateCommentCommand(parentId, content, mentions == null ? List.of() : mentions);
    }
}
