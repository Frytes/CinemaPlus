package com.frytes.cinemaPlus.users;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.users.entity.RefreshToken;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.RefreshTokenRepository;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import com.frytes.cinemaPlus.users.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("🔄 Интеграционный тест Refresh Token Service")
class RefreshTokenServiceTest extends BaseIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.refresh-expiration-days}")
    private int refreshDurationDays;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("✅ Должен создать Refresh Token в БД с правильным сроком жизни")
    void shouldCreateRefreshToken_WithCorrectExpiry() {
        User user = new User(null, "refresher", "refresh@test.com", "password123", Role.USER);
        userRepository.save(user);

        RefreshToken createdToken = refreshTokenService.createRefreshToken(user.getEmail());

        assertThat(createdToken).isNotNull();
        assertThat(createdToken.getToken()).isNotBlank();
        assertThat(createdToken.getUser().getEmail()).isEqualTo("refresh@test.com");

        Optional<RefreshToken> tokenFromDb = refreshTokenRepository.findByToken(createdToken.getToken());
        assertThat(tokenFromDb).isPresent();

        LocalDateTime expectedExpiry = LocalDateTime.now().plusDays(refreshDurationDays);

        assertThat(tokenFromDb.get().getExpiryDate())
                .as("Дата истечения должна быть через %d дней", refreshDurationDays)
                .isCloseTo(expectedExpiry, within(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("❌ Должен удалить токен, если он просрочен (verifyExpiration)")
    void shouldDeleteToken_IfExpired() {
        User user = userRepository.save(new User(null, "expiredUser", "old@test.com", "password123", Role.USER));
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(user);
        expiredToken.setToken("old-token-uuid");
        expiredToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        refreshTokenRepository.save(expiredToken);


        try {
            refreshTokenService.verifyExpiration(expiredToken);
        } catch (Exception e) {
            // Ожидаем ошибку TokenRefreshException
        }

        assertThat(refreshTokenRepository.findByToken("old-token-uuid")).isEmpty();
    }
}