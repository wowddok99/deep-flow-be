package com.deepflow.infra.security;

import com.deepflow.application.port.out.auth.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider implements TokenProvider {

    private final SecretKey key;
    private final JwtParser jwtParser;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parser().verifyWith(this.key).build();
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    @Override
    public String createAccessToken(String username, String role, Long userId) {
        return createToken(username, role, userId, accessTokenValidityInMilliseconds, TOKEN_TYPE_ACCESS);
    }

    @Override
    public String createRefreshToken(String username) {
        return createToken(username, null, null, refreshTokenValidityInMilliseconds, TOKEN_TYPE_REFRESH);
    }

    private String createToken(String username, String role, Long userId, long validity, String tokenType) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + validity);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(validityDate)
                .signWith(key);

        if (role != null) {
            builder.claim("role", role);
        }

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder.compact();
    }

    @Override
    public Optional<TokenClaims> parseAndValidate(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return Optional.of(new TokenClaims(
                    claims.getSubject(),
                    claims.get("userId", Long.class),
                    claims.get("role", String.class),
                    claims.get("type", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
