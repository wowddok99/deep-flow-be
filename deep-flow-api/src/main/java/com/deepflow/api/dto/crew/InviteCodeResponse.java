package com.deepflow.api.dto.crew;

import com.deepflow.application.crew.dto.InviteCodeIssuedInfo;

import java.time.LocalDateTime;

public record InviteCodeResponse(
        String code,
        LocalDateTime expiresAt
) {
    public static InviteCodeResponse from(InviteCodeIssuedInfo info) {
        return new InviteCodeResponse(info.code(), info.expiresAt());
    }
}
