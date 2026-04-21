package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CrewMaxMembersBelowCurrentException extends CustomException {
    public CrewMaxMembersBelowCurrentException() {
        super(ErrorCode.CREW_MAX_MEMBERS_BELOW_CURRENT);
    }
}
