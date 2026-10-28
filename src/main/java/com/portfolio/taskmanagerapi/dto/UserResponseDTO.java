package com.portfolio.taskmanagerapi.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email
) {}