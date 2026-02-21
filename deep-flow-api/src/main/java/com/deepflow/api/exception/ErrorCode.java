package com.deepflow.api.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
public enum ErrorCode {

    // Auth
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "Username already exists"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired token"),

    // Session
    SESSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "An ongoing session already exists"),
    SESSION_NOT_DELETABLE(HttpStatus.CONFLICT, "Cannot delete an ongoing session"),

    // Common
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
