package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionAlreadyExistsException extends CustomException {

    public SessionAlreadyExistsException() {
        super(ErrorCode.SESSION_ALREADY_EXISTS);
    }
}
