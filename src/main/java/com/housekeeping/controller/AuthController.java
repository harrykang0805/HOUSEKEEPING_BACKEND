package com.housekeeping.controller;

import com.housekeeping.DTO.UserDTO;
import com.housekeeping.entity.LevelEXPTable;
import com.housekeeping.entity.User;
import com.housekeeping.repository.LevelEXPTableRepository;
import com.housekeeping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final LevelEXPTableRepository levelEXPTableRepository;

    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration(@RequestBody UserDTO userDTO, @RequestHeader("Authorization") String token) {
        Optional<User> existingUser = userRepository.findByNickname(userDTO.getNickname());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(400).body("Nickname already exists");
        }

        Optional<LevelEXPTable> defaultLevelOpt = levelEXPTableRepository.findByLevelLevel(1);

        if (defaultLevelOpt.isEmpty()) {
            return ResponseEntity.status(500).body("Default level not found");
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .nickname(userDTO.getNickname())
                .phoneNumber(userDTO.getPhoneNumber())
                .userPlatform(userDTO.getUserPlatform())
                .role(userDTO.getRole())
                .userEnrollment(LocalDateTime.now())
                .level(defaultLevelOpt.get())
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("Registration completed");
    }
}