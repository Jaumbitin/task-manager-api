package com.portfolio.taskmanagerapi.dto;

import com.portfolio.taskmanagerapi.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;

public record TaskRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        String title,

        String description,

        TaskStatus status
) {}