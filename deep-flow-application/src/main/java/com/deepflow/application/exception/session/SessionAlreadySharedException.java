package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionAlreadySharedException extends CustomException {
    public SessionAlreadySharedException() {
        super(ErrorCode.SESSION_ALREADY_SHARED);
    }
}
