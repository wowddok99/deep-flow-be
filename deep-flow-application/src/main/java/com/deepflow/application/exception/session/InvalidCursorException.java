package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class InvalidCursorException extends CustomException {
    public InvalidCursorException() {
        super(ErrorCode.INVALID_CURSOR);
    }
}
