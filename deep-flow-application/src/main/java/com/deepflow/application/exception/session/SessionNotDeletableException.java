package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionNotDeletableException extends CustomException {

    public SessionNotDeletableException() {
        super(ErrorCode.SESSION_NOT_DELETABLE);
    }
}
