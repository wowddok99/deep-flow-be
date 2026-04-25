package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SessionNotInCrewException extends CustomException {
    public SessionNotInCrewException() {
        super(ErrorCode.SESSION_NOT_IN_CREW);
    }
}
