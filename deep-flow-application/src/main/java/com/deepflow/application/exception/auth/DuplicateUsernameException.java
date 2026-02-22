package com.deepflow.application.exception.auth;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class DuplicateUsernameException extends CustomException {

    public DuplicateUsernameException() {
        super(ErrorCode.DUPLICATE_USERNAME);
    }

    public DuplicateUsernameException(String username) {
        super(ErrorCode.DUPLICATE_USERNAME, "Username already exists: " + username);
    }
}
