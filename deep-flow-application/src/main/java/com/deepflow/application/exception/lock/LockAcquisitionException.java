package com.deepflow.application.exception.lock;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class LockAcquisitionException extends CustomException {

    public LockAcquisitionException() {
        super(ErrorCode.LOCK_ACQUISITION_FAILED);
    }
}
