package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CrewNotFoundException extends CustomException {
    public CrewNotFoundException() {
        super(ErrorCode.CREW_NOT_FOUND);
    }
}
