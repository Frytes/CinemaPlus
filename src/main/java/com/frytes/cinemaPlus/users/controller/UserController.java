package com.frytes.cinemaPlus.users.controller;

import com.frytes.cinemaPlus.users.dto.ChangePasswordRequest;
import com.frytes.cinemaPlus.users.dto.UpdateProfileRequest;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        userService.updateProfile(user, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        userService.changePassword(user, request);
        return ResponseEntity.ok().build();
    }
}