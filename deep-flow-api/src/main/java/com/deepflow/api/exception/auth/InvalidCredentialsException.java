package com.deepflow.api.exception.auth;

import com.deepflow.api.exception.CustomException;
import com.deepflow.api.exception.ErrorCode;

/**
 * 로그인 실패 시 발생 (잘못된 username 또는 password)
 */
public class InvalidCredentialsException extends CustomException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
