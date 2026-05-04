package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class NotCrewMemberException extends CustomException {
    public NotCrewMemberException() {
        super(ErrorCode.NOT_CREW_MEMBER);
    }
}
