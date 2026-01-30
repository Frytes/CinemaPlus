package com.frytes.cinemaPlus.users.service;

import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.common.exception.UserAlreadyExistsException;
import com.frytes.cinemaPlus.common.service.MessageBroker;
import com.frytes.cinemaPlus.users.dto.*;
import com.frytes.cinemaPlus.users.entity.RefreshToken;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.event.UserRegisteredEvent;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final MessageBroker messageBroker;

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        User user = userMapper.toEntity(request);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );
        try {
            messageBroker.send("user-events-topic", user.getEmail(), event);
            log.info("Событие регистрации отправлено в Kafka");
        } catch (Exception e) {
            log.error("Ошибка отправки в Kafka, но регистрация прошла успешно", e);
        }
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()
                )
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken.getToken());
    }
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenService.processRefresh(request.refreshToken());
    }

}