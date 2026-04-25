package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class TagLimitExceededException extends CustomException {
    public TagLimitExceededException() {
        super(ErrorCode.TAG_LIMIT_EXCEEDED);
    }
}
