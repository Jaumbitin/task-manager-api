package com.portfolio.taskmanagerapi.service;

import com.portfolio.taskmanagerapi.domain.User;
import com.portfolio.taskmanagerapi.dto.UserRequestDTO;
import com.portfolio.taskmanagerapi.dto.UserResponseDTO;
import com.portfolio.taskmanagerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(UserRequestDTO dto) {
        if (dto.password().length() < 8) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Use uma senha com pelo menos 8 caracteres");
        // Nao permite e-mails duplicados
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Este email ja esta cadastrado");
        }

        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(savedUser.getId(), savedUser.getEmail());
    }
}
