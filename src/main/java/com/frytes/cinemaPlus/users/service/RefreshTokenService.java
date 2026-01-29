package com.frytes.cinemaPlus.users.service;

import com.frytes.cinemaPlus.common.exception.TokenRefreshException;
import com.frytes.cinemaPlus.users.dto.TokenRefreshResponse;
import com.frytes.cinemaPlus.users.entity.RefreshToken;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.RefreshTokenRepository;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration-days:30}")
    private int refreshDurationDays;

    public RefreshToken createRefreshToken(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return refreshTokenRepository.save(
                RefreshToken.builder()
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(refreshDurationDays))
                .token(UUID.randomUUID().toString())
                .build());
    }
    public void verifyExpiration(RefreshToken token){
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh токен истек. Пожалуйста, войдите снова.");
        }

    }
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh токен не найден в базе"));
    }

    @Transactional
    public TokenRefreshResponse processRefresh(String requestRefreshToken) {
        RefreshToken token = findByToken(requestRefreshToken);
        verifyExpiration(token);
        User user = token.getUser();
        refreshTokenRepository.delete(token);
        RefreshToken newRefreshToken = createRefreshToken(user.getEmail());
        String newAccessToken = jwtService.generateToken(user);
        return new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
    }
}
