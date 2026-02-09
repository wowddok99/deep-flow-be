package com.deepflow.api.exception.auth;

import com.deepflow.api.exception.CustomException;
import com.deepflow.api.exception.ErrorCode;

/**
 * 유효하지 않거나 만료된 토큰 사용 시 발생
 */
public class InvalidTokenException extends CustomException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
