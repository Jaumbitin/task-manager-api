package com.portfolio.taskmanagerapi.dto;

import com.portfolio.taskmanagerapi.domain.TaskStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt
) {}