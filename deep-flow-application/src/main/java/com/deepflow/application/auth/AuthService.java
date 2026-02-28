package com.deepflow.application.auth;

import com.deepflow.application.auth.port.TokenProvider;
import com.deepflow.application.exception.auth.DuplicateUsernameException;
import com.deepflow.application.exception.auth.InvalidCredentialsException;
import com.deepflow.application.exception.auth.InvalidTokenException;
import com.deepflow.domain.user.Role;
import com.deepflow.domain.user.User;
import com.deepflow.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public void signup(String username, String password, String name) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("회원가입 실패 - 중복 username: {}", username);
            throw new DuplicateUsernameException(username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("회원가입 완료: username={}", username);
    }

    @Transactional
    public TokenResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 계정: {}", username);
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: userId={}", user.getId());
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String refreshToken = tokenProvider.createRefreshToken(user.getUsername());

        user.updateRefreshToken(refreshToken);
        user.afterLogin();

        log.info("로그인 성공: userId={}", user.getId());
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            log.warn("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰");
            throw new InvalidTokenException();
        }

        String username = tokenProvider.getUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("토큰 갱신 실패 - 사용자 없음: username={}", username);
                    return new InvalidTokenException();
                });

        if (!refreshToken.equals(user.getRefreshToken())) {
            log.warn("토큰 갱신 실패 - 저장된 토큰과 불일치: username={}", username);
            throw new InvalidTokenException();
        }

        String newAccessToken = tokenProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getUsername());

        user.updateRefreshToken(newRefreshToken);

        log.info("토큰 갱신: userId={}", user.getId());
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            return;
        }
        String username = tokenProvider.getUsername(refreshToken);
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.updateRefreshToken(null);
                    log.info("로그아웃: username={}", username);
                });
    }

    public record TokenResponse(String accessToken, String refreshToken) {
    }
}
