package com.deepflow.api.controller.auth;

import com.deepflow.api.dto.*;
import com.deepflow.api.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@Tag(name = "Authentication", description = "User authentication API")
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

    @Operation(summary = "Sign up")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> signup(@RequestBody @Valid SignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok());
    }

    @Operation(summary = "Login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        // 로그인 처리 후 access/refresh 토큰 발급
        AuthService.TokenResponse tokenResponse = authService.login(request);

        // refresh 토큰을 HttpOnly 쿠키로 생성
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());

        // access 토큰은 응답 바디에 포함,
        // refresh 토큰은 Set-Cookie 헤더로 전달
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(CommonResponse.ok(new LoginResponse(tokenResponse.accessToken())));
    }

    @Operation(summary = "Reissue access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token reissued"),
            @ApiResponse(responseCode = "400", description = "Missing refresh token"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<LoginResponse>> reissue(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        // refresh 토큰이 없으면 400 반환
        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(CommonResponse.error(new ApiError("MISSING_TOKEN", "Refresh token is required")));
        }

        // refresh 토큰 검증 후 access/refresh 토큰 재발급
        AuthService.TokenResponse tokenResponse = authService.reissue(refreshToken);

        // 새 refresh 토큰을 HttpOnly 쿠키로 재설정 (rotation)
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());

        // access 토큰은 응답 바디에 포함,
        // refresh 토큰은 Set-Cookie 헤더로 전달
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(CommonResponse.ok(new LoginResponse(tokenResponse.accessToken())));
    }

    @Operation(summary = "Logout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        // refresh 토큰이 있으면 DB에 저장된 토큰 값 제거
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // 클라이언트에 저장된 refresh 토큰 쿠키 삭제 (maxAge=0)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(CommonResponse.ok());
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
