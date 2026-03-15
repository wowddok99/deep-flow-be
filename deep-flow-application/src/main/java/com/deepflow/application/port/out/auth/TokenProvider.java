package com.deepflow.application.port.out.auth;

public interface TokenProvider {

    String createAccessToken(String username, String role, Long userId);

    String createRefreshToken(String username);

    boolean validateToken(String token);

    String getUsername(String token);

    Long getUserId(String token);
}
