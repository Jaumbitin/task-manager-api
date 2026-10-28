package com.portfolio.taskmanagerapi.repository;

import com.portfolio.taskmanagerapi.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Traz apenas as tarefas de um usuário específico (essencial para a segurança)
    List<Task> findByUserId(UUID userId);
    java.util.Optional<Task> findByIdAndUserId(UUID id, UUID userId);
}