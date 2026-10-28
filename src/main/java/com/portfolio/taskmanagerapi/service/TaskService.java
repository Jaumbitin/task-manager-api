package com.portfolio.taskmanagerapi.service;

import com.portfolio.taskmanagerapi.domain.Task;
import com.portfolio.taskmanagerapi.domain.User;
import com.portfolio.taskmanagerapi.dto.TaskRequestDTO;
import com.portfolio.taskmanagerapi.dto.TaskResponseDTO;
import com.portfolio.taskmanagerapi.repository.TaskRepository;
import com.portfolio.taskmanagerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponseDTO createTask(TaskRequestDTO dto, UUID userId) {
        // 1. Valida se o usuário dono da tarefa existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        // 2. Converte o DTO que chegou na requisição para a Entidade que vai para o banco
        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .status(dto.status())
                .user(user)
                .build();

        // 3. Salva no PostgreSQL
        Task savedTask = taskRepository.save(task);

        // 4. Retorna a Entidade convertida no DTO de resposta seguro
        return mapToResponseDTO(savedTask);
    }

    public List<TaskResponseDTO> listTasksByUser(UUID userId) {
        // Busca tarefas filtradas pelo ID do usuário e converte a lista inteira para DTOs
        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Método utilitário para isolar a lógica de conversão (Anti-Vibe Coding)
    public TaskResponseDTO updateTask(UUID taskId, UUID userId, TaskRequestDTO dto) {
        Task task = ownedTask(taskId, userId);
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        if (dto.status() != null) task.setStatus(dto.status());
        return mapToResponseDTO(taskRepository.save(task));
    }

    public TaskResponseDTO updateStatus(UUID taskId, UUID userId, com.portfolio.taskmanagerapi.domain.TaskStatus status) {
        Task task = ownedTask(taskId, userId);
        task.setStatus(status);
        return mapToResponseDTO(taskRepository.save(task));
    }

    public void deleteTask(UUID taskId, UUID userId) {
        taskRepository.delete(ownedTask(taskId, userId));
    }

    private Task ownedTask(UUID taskId, UUID userId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Tarefa nao encontrada"));
    }
    private TaskResponseDTO mapToResponseDTO(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}