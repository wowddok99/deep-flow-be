package com.deepflow.application.exception.auth;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class InvalidTokenException extends CustomException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
