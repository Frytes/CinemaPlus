package com.frytes.cinemaPlus.users.service;

import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.common.exception.UserAlreadyExistsException;
import com.frytes.cinemaPlus.users.dto.LoginRequest;
import com.frytes.cinemaPlus.users.dto.RegisterRequest;
import com.frytes.cinemaPlus.users.dto.UserMapper;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        userRepository.save(user);
        return  jwtService.generateToken(user);
    }
    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return jwtService.generateToken(user);
    }


}