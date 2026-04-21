package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class InvalidInviteCodeException extends CustomException {
    public InvalidInviteCodeException() {
        super(ErrorCode.INVALID_INVITE_CODE);
    }
}
