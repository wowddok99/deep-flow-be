package com.deepflow.application.exception.crew;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class CrewNotPublicException extends CustomException {
    public CrewNotPublicException() {
        super(ErrorCode.CREW_NOT_PUBLIC);
    }
}
