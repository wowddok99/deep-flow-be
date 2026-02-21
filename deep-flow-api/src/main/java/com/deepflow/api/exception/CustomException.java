package com.deepflow.api.exception;

/**
 * 커스텀 예외의 기본 클래스
 */
public abstract class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    protected CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
