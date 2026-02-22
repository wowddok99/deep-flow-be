package com.deepflow.application.auth;

import com.deepflow.application.auth.port.TokenProvider;
import com.deepflow.application.exception.auth.DuplicateUsernameException;
import com.deepflow.application.exception.auth.InvalidCredentialsException;
import com.deepflow.application.exception.auth.InvalidTokenException;
import com.deepflow.domain.user.Role;
import com.deepflow.domain.user.User;
import com.deepflow.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public void signup(String username, String password, String name) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String refreshToken = tokenProvider.createRefreshToken(user.getUsername());

        user.updateRefreshToken(refreshToken);
        user.afterLogin();

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        String username = tokenProvider.getUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidTokenException::new);

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        String newAccessToken = tokenProvider.createAccessToken(user.getUsername(), user.getRole().name(), user.getId());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getUsername());

        user.updateRefreshToken(newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            return;
        }
        String username = tokenProvider.getUsername(refreshToken);
        userRepository.findByUsername(username)
                .ifPresent(user -> user.updateRefreshToken(null));
    }

    public record TokenResponse(String accessToken, String refreshToken) {
    }
}
