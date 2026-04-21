package com.deepflow.application.crew.dto;

import java.time.LocalDateTime;

public record InviteCodeIssuedInfo(
        String code,
        LocalDateTime expiresAt
) {}
