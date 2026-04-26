package com.deepflow.application.session.dto;

import java.io.Serializable;
import java.util.List;

public record CrewHighlightInfo(
        HighlightMode mode,
        List<HighlightItemInfo> items,
        int recentSharedCount,
        int threshold
) implements Serializable {}
