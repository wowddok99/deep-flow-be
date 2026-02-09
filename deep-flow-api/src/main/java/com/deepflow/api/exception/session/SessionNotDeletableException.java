package com.deepflow.api.exception.session;

import com.deepflow.api.exception.CustomException;
import com.deepflow.api.exception.ErrorCode;

/**
 * 진행 중인 세션을 삭제하려고 할 때 발생
 */
public class SessionNotDeletableException extends CustomException {

    public SessionNotDeletableException() {
        super(ErrorCode.SESSION_NOT_DELETABLE);
    }
}
