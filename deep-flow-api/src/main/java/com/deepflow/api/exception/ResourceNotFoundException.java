package com.deepflow.api.exception;

public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
