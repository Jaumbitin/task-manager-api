package com.portfolio.taskmanagerapi.repository;

import com.portfolio.taskmanagerapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // O Spring Data JPA entende o nome do método e cria o SQL automaticamente:
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}