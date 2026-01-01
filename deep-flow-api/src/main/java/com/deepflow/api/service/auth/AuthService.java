package com.deepflow.api.service.auth;

import com.deepflow.api.dto.*;
import com.deepflow.core.security.JwtProvider;
import com.deepflow.core.domain.user.Role;
import com.deepflow.core.domain.user.User;
import com.deepflow.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignUpRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername());

        user.updateRefreshToken(refreshToken);
        user.afterLogin();

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 저장된 토큰과 요청된 토큰이 일치하는지 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUsername());
        
        // 리프레시 토큰 교체 및 저장
        user.updateRefreshToken(newRefreshToken);
        
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
           return; // 이미 유효하지 않으면 무시
        }
        String username = jwtProvider.getUsername(refreshToken);
        userRepository.findByUsername(username)
                .ifPresent(user -> user.updateRefreshToken(null));
    }

    public record TokenResponse(String accessToken, String refreshToken) {}
}
