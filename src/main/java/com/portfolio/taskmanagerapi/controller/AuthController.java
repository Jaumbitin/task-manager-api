package com.portfolio.taskmanagerapi.controller;

import com.portfolio.taskmanagerapi.domain.User;
import com.portfolio.taskmanagerapi.dto.UserRequestDTO;
import com.portfolio.taskmanagerapi.repository.UserRepository;
import com.portfolio.taskmanagerapi.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@jakarta.validation.Valid @RequestBody UserRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

        // Se a senha bater, gera e devolve o token JWT
        if (passwordEncoder.matches(dto.password(), user.getPassword())) {
            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(401).body("Senha inválida");
    }
}
