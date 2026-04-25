package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionNotSharedException extends CustomException {
    public SessionNotSharedException() {
        super(ErrorCode.SESSION_NOT_SHARED);
    }
}
