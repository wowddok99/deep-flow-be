package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CrewAccessDeniedException extends CustomException {
    public CrewAccessDeniedException() {
        super(ErrorCode.CREW_ACCESS_DENIED);
    }
}
