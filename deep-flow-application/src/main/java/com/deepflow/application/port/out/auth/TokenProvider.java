package com.deepflow.application.port.out.auth;

import org.springframework.lang.Nullable;

import java.util.Optional;

public interface TokenProvider {

    String TOKEN_TYPE_ACCESS = "access";
    String TOKEN_TYPE_REFRESH = "refresh";

    String createAccessToken(String username, String role, Long userId);

    String createRefreshToken(String username);

    Optional<TokenClaims> parseAndValidate(String token);

    record TokenClaims(String username, @Nullable Long userId, @Nullable String role, String type) {
        public boolean isAccess()  { return TOKEN_TYPE_ACCESS.equals(type); }
        public boolean isRefresh() { return TOKEN_TYPE_REFRESH.equals(type); }
    }
}
