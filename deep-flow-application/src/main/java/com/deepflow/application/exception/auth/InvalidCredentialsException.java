package com.deepflow.application.exception.auth;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class InvalidCredentialsException extends CustomException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
