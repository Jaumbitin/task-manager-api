package com.portfolio.taskmanagerapi.controller;
import com.portfolio.taskmanagerapi.domain.User;
import com.portfolio.taskmanagerapi.dto.*;
import com.portfolio.taskmanagerapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@AuthenticationPrincipal User user, @Valid @RequestBody TaskRequestDTO dto) {
        return ResponseEntity.status(201).body(taskService.createTask(dto, user.getId()));
    }
    @GetMapping
    public List<TaskResponseDTO> list(@AuthenticationPrincipal User user) {
        return taskService.listTasksByUser(user.getId());
    }
    @PostMapping("/{userId}")
    public ResponseEntity<TaskResponseDTO> createForUser(@PathVariable UUID userId, @AuthenticationPrincipal User user, @Valid @RequestBody TaskRequestDTO dto) {
        requireOwner(userId, user);
        return create(user, dto);
    }
    @GetMapping("/user/{userId}")
    public List<TaskResponseDTO> listForUser(@PathVariable UUID userId, @AuthenticationPrincipal User user) {
        requireOwner(userId, user);
        return list(user);
    }
    @PutMapping("/{taskId}")
    public TaskResponseDTO update(@PathVariable UUID taskId, @AuthenticationPrincipal User user, @Valid @RequestBody TaskRequestDTO dto) {
        return taskService.updateTask(taskId, user.getId(), dto);
    }
    @PatchMapping("/{taskId}/status")
    public TaskResponseDTO status(@PathVariable UUID taskId, @AuthenticationPrincipal User user, @Valid @RequestBody TaskStatusRequestDTO dto) {
        return taskService.updateStatus(taskId, user.getId(), dto.status());
    }
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable UUID taskId, @AuthenticationPrincipal User user) {
        taskService.deleteTask(taskId, user.getId());
        return ResponseEntity.noContent().build();
    }
    private void requireOwner(UUID userId, User user) {
        if (!user.getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
    }
}
