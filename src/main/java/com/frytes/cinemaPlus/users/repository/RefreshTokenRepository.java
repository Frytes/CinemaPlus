package com.frytes.cinemaPlus.users.repository;

import com.frytes.cinemaPlus.users.entity.RefreshToken;
import com.frytes.cinemaPlus.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Modifying
    int deleteByUser(User user);
}
