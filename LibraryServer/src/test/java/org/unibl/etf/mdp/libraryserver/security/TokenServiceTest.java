package org.unibl.etf.mdp.libraryserver.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {
    private static final byte[] SECRET = "a-test-secret-of-sufficient-length".getBytes(StandardCharsets.UTF_8);
    private static final Instant NOW = Instant.parse("2024-03-15T10:00:00Z");

    private static TokenService at(Instant now) {
        return new TokenService(SECRET, Duration.ofMinutes(30), Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void tokenCarriesUsernameAndRole() {
        String token = at(NOW).issue("marko", Roles.MEMBER);

        assertThat(at(NOW).verify(token)).hasValueSatisfying(c -> {
            assertThat(c.getUsername()).isEqualTo("marko");
            assertThat(c.getRole()).isEqualTo(Roles.MEMBER);
        });
    }

    @Test
    void tokenExpires() {
        String token = at(NOW).issue("marko", Roles.MEMBER);

        assertThat(at(NOW.plus(Duration.ofMinutes(29))).verify(token)).isPresent();
        assertThat(at(NOW.plus(Duration.ofMinutes(31))).verify(token)).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        TokenService other = new TokenService("another-secret-another-secret!!".getBytes(StandardCharsets.UTF_8),
                Duration.ofMinutes(30), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(at(NOW).verify(other.issue("marko", Roles.ADMIN))).isEmpty();
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = at(NOW).issue("marko", Roles.MEMBER);
        String[] parts = token.split("\\.");
        // Swap in a payload that claims the admin role, keeping the old signature
        String forged = parts[0] + "." + at(NOW).issue("marko", Roles.ADMIN).split("\\.")[1] + "." + parts[2];

        assertThat(at(NOW).verify(forged)).isEmpty();
        assertThat(at(NOW).verify("not-a-token")).isEmpty();
        assertThat(at(NOW).verify("")).isEmpty();
        assertThat(at(NOW).verify(null)).isEmpty();
    }
}
