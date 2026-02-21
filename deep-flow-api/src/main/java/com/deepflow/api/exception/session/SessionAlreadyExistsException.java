package com.deepflow.api.exception.session;

import com.deepflow.api.exception.CustomException;
import com.deepflow.api.exception.ErrorCode;

/**
 * 이미 진행 중인 세션이 있을 때 새 세션 시작 시도 시 발생
 */
public class SessionAlreadyExistsException extends CustomException {

    public SessionAlreadyExistsException() {
        super(ErrorCode.SESSION_ALREADY_EXISTS);
    }
}
