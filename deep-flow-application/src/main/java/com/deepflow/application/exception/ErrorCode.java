package com.deepflow.application.exception;

public enum ErrorCode {

    // Auth
    DUPLICATE_USERNAME(409, "Username already exists"),
    INVALID_CREDENTIALS(401, "Invalid username or password"),
    INVALID_TOKEN(401, "Invalid or expired token"),

    // Session
    SESSION_ALREADY_EXISTS(409, "An ongoing session already exists"),
    SESSION_NOT_DELETABLE(409, "Cannot delete an ongoing session"),

    // Lock
    LOCK_ACQUISITION_FAILED(409, "Another request is being processed"),

    // Common
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    INTERNAL_ERROR(500, "Internal server error");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
