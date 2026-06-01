package com.deepflow.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T>(
        boolean success,
        T data,
        ApiError error
) {
    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(true, data, null);
    }

    public static CommonResponse<Void> ok() {
        return new CommonResponse<>(true, null, null);
    }

    public static <T> CommonResponse<T> error(ApiError error) {
        return new CommonResponse<>(false, null, error);
    }
}
