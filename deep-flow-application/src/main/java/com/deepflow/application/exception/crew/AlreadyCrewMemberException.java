package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class AlreadyCrewMemberException extends CustomException {
    public AlreadyCrewMemberException() {
        super(ErrorCode.ALREADY_CREW_MEMBER);
    }
}
