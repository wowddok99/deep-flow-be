package com.deepflow.api.dto;

public record ApiError(
        String code,
        String message
) {
}
