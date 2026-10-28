package com.portfolio.taskmanagerapi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.portfolio.taskmanagerapi.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {
    private final Algorithm algorithm;

    public TokenService(@Value("${api.security.token.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("JWT_SECRET deve conter pelo menos 32 bytes");
        algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(User user) {
        return JWT.create()
                .withIssuer("task-manager-api")
                .withSubject(user.getEmail())
                .withClaim("userId", user.getId().toString())
                .withExpiresAt(Instant.now().plus(Duration.ofHours(2)))
                .sign(algorithm);
    }

    public String validateToken(String token) {
        try {
            var decoded = JWT.require(algorithm).withIssuer("task-manager-api").build().verify(token);
            return decoded.getExpiresAt() != null && decoded.getSubject() != null ? decoded.getSubject() : "";
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            return "";
        }
    }
}
