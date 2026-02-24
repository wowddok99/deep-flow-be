package com.deepflow.application.common;

import java.util.List;

public record SliceResult<T>(List<T> content, Long nextCursorId, boolean hasNext) {}
