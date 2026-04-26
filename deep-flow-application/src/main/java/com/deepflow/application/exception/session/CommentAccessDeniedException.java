package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CommentAccessDeniedException extends CustomException {
    public CommentAccessDeniedException() {
        super(ErrorCode.COMMENT_ACCESS_DENIED);
    }
}
