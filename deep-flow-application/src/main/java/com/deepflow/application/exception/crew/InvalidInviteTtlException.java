package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class InvalidInviteTtlException extends CustomException {
    public InvalidInviteTtlException() {
        super(ErrorCode.INVALID_INVITE_TTL);
    }
}
