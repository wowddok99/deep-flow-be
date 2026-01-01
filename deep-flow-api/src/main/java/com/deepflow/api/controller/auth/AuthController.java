package com.deepflow.api.controller.auth;

import com.deepflow.api.dto.*;
import com.deepflow.api.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.auth.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie.same-site}")
    private String cookieSameSite;

    @Value("${app.auth.cookie.max-age}")
    private long cookieMaxAge;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        AuthService.TokenResponse tokenResponse = authService.login(request);
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(tokenResponse.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthService.TokenResponse tokenResponse = authService.reissue(refreshToken);
        
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(tokenResponse.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        // 1. 서버 측 저장소(DB)에서 토큰 삭제 로직 호출
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // 2. 클라이언트 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(cookieMaxAge)
                .sameSite(cookieSameSite)
                .build();
    }
}
