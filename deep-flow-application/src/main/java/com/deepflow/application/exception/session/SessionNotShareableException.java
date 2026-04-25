package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionNotShareableException extends CustomException {
    public SessionNotShareableException() {
        super(ErrorCode.SESSION_NOT_SHAREABLE);
    }
}
