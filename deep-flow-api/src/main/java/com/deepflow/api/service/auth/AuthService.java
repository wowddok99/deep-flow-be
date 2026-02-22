package com.deepflow.api.service.auth;

import com.deepflow.api.dto.*;
import com.deepflow.api.exception.auth.DuplicateUsernameException;
import com.deepflow.api.exception.auth.InvalidCredentialsException;
import com.deepflow.api.exception.auth.InvalidTokenException;
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
            throw new DuplicateUsernameException(request.username());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password())) // 비밀번호 암호화
                .name(request.name())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 유저 조회
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername());

        user.updateRefreshToken(refreshToken);
        user.afterLogin();

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // 서명 및 만료 여부 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        String username = jwtProvider.getUsername(refreshToken);

        // 사용자 조회, 존재하지 않으면 인증 실패 처리
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidTokenException::new);

        // DB에 저장된 refresh 토큰과 일치하는지 확인
        // 이미 교체된 토큰이면 거부하여 탈취 재사용 방지
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        // 새로운 access/refresh 토큰 생성
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

    public record TokenResponse(String accessToken, String refreshToken) {
    }
}
