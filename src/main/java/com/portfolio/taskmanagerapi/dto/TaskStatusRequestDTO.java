package com.portfolio.taskmanagerapi.dto;
import com.portfolio.taskmanagerapi.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;
public record TaskStatusRequestDTO(@NotNull TaskStatus status) {}
