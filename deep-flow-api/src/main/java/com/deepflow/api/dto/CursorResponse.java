package com.deepflow.api.dto;

import java.util.List;

public record CursorResponse<T>(
    List<T> content,
    Long nextCursorId,
    boolean hasNext
) {}
