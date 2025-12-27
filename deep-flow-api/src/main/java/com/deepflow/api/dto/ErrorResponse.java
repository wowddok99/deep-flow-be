package com.deepflow.api.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp) {
    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
