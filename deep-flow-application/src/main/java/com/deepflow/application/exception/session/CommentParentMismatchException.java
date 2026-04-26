package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CommentParentMismatchException extends CustomException {
    public CommentParentMismatchException() {
        super(ErrorCode.COMMENT_PARENT_MISMATCH);
    }
}
