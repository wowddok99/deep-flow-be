package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CrewMemberLimitExceededException extends CustomException {
    public CrewMemberLimitExceededException() {
        super(ErrorCode.CREW_MEMBER_LIMIT_EXCEEDED);
    }
}
