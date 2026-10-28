package com.portfolio.taskmanagerapi.controller;

import com.portfolio.taskmanagerapi.dto.UserRequestDTO;
import com.portfolio.taskmanagerapi.repository.UserRepository;
import com.portfolio.taskmanagerapi.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public AuthController(UserRepository users, TokenService tokens, PasswordEncoder encoder) {
        this.userRepository = users;
        this.tokenService = tokens;
        this.passwordEncoder = encoder;
        this.dummyHash = encoder.encode(java.util.UUID.randomUUID().toString());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserRequestDTO dto) {
        var user = userRepository.findByEmail(dto.email());
        // Execute BCrypt even for an unknown email and return the same error.
        boolean matches = passwordEncoder.matches(dto.password(), user.map(u -> u.getPassword()).orElse(dummyHash));
        if (user.isEmpty() || !matches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
        return ResponseEntity.ok(tokenService.generateToken(user.get()));
    }
}
