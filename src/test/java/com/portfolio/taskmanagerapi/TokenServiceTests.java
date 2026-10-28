package com.portfolio.taskmanagerapi;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.portfolio.taskmanagerapi.domain.User;
import com.portfolio.taskmanagerapi.security.TokenService;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class TokenServiceTests {
    private final String secret = "test-only-secret-with-at-least-32-bytes";
    private final TokenService tokens = new TokenService(secret);

    @Test void expirationIsIndependentOfSystemTimezone() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
            var user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
            Instant before = Instant.now();
            String token = tokens.generateToken(user);
            assertThat(JWT.decode(token).getExpiresAtAsInstant()).isBetween(before.plusSeconds(7198), before.plusSeconds(7202));
            assertThat(tokens.validateToken(token)).isEqualTo(user.getEmail());
        } finally { TimeZone.setDefault(original); }
    }

    @Test void rejectsExpiredMissingClaimsAndWrongSignatures() {
        var key = Algorithm.HMAC256(secret);
        assertThat(tokens.validateToken(JWT.create().withIssuer("task-manager-api").withSubject("test@example.com")
                .withExpiresAt(Instant.now().minusSeconds(1)).sign(key))).isEmpty();
        assertThat(tokens.validateToken(JWT.create().withIssuer("task-manager-api").withSubject("test@example.com").sign(key))).isEmpty();
        assertThat(tokens.validateToken(JWT.create().withIssuer("task-manager-api").withExpiresAt(Instant.now().plusSeconds(60)).sign(key))).isEmpty();
        assertThat(tokens.validateToken(JWT.create().withIssuer("task-manager-api").withSubject("test@example.com")
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256("another-key")))).isEmpty();
    }

    @Test void rejectsWeakConfiguration() {
        assertThatThrownBy(() -> new TokenService("short")).isInstanceOf(IllegalStateException.class);
    }
}
