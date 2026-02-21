package com.deepflow.api.exception.auth;

import com.deepflow.api.exception.CustomException;
import com.deepflow.api.exception.ErrorCode;

/**
 * 중복된 username으로 회원가입 시도 시 발생
 */
public class DuplicateUsernameException extends CustomException {

    public DuplicateUsernameException() {
        super(ErrorCode.DUPLICATE_USERNAME);
    }

    public DuplicateUsernameException(String username) {
        super(ErrorCode.DUPLICATE_USERNAME, "Username already exists: " + username);
    }
}
