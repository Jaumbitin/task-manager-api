package com.portfolio.taskmanagerapi.dto;

import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;

public record UserRequestDTO(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
    @AssertTrue(message = "A senha deve ter no máximo 72 bytes em UTF-8")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
