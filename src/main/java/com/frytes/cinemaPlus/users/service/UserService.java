package com.frytes.cinemaPlus.users.service;

import com.frytes.cinemaPlus.common.exception.UserAlreadyExistsException;
import com.frytes.cinemaPlus.users.dto.ChangePasswordRequest;
import com.frytes.cinemaPlus.users.dto.UpdateProfileRequest;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updateProfile(User user, UpdateProfileRequest request) {
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email уже занят");
        }

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Имя пользователя занято");
        }
        user.setUsername(request.username());
        user.setEmail(request.email());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Неверный текущий пароль");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}