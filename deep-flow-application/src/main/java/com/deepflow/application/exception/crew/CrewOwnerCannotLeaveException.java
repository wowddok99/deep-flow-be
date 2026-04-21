package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CrewOwnerCannotLeaveException extends CustomException {
    public CrewOwnerCannotLeaveException() {
        super(ErrorCode.CREW_OWNER_CANNOT_LEAVE);
    }
}
