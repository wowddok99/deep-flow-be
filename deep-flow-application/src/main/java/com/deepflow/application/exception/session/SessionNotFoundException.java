package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionNotFoundException extends CustomException {
    public SessionNotFoundException() {
        super(ErrorCode.SESSION_NOT_FOUND);
    }
}
