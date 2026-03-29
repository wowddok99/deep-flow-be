package com.deepflow.api.security;

import com.deepflow.application.port.out.auth.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            Optional<TokenProvider.TokenClaims> parsed = tokenProvider.parseAndValidate(token);

            if (parsed.isEmpty()) {
                log.warn("JWT 파싱 실패 (만료/위변조): IP={}", request.getRemoteAddr());
            } else if (!parsed.get().isAccess()) {
                log.warn("Access 토큰이 아닌 토큰으로 인증 시도: IP={}", request.getRemoteAddr());
            } else {
                TokenProvider.TokenClaims claims = parsed.get();
                UserDetails userDetails = new CustomUserDetails(
                        claims.username(), "", Collections.emptyList(), claims.userId());
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 인증 성공: userId={}", claims.userId());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // Authorization 헤더에서 Bearer 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // SSE 엔드포인트용: query parameter에서 토큰 추출
        // 브라우저 EventSource API는 커스텀 헤더를 지원하지 않으므로 ?token= 방식 지원
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }
}
