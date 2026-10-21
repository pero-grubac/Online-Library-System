package org.unibl.etf.mdp.libraryserver.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

/** Issues and checks the signed (HS256) session tokens that clients send as {@code Authorization: Bearer}. */
public class TokenService {
    private static final String ISSUER = "library-server";
    private static final String ROLE_CLAIM = "role";
    private static final Logger logger = FileLogger.getLogger(TokenService.class.getName());
    private static TokenService instance;

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final Duration ttl;
    private final Clock clock;

    public TokenService(byte[] secret, Duration ttl, Clock clock) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.ttl = ttl;
        this.clock = clock;
        // The verifier checks expiry against the same clock that issues tokens
        this.verifier = ((JWTVerifier.BaseVerification) JWT.require(algorithm).withIssuer(ISSUER)).build(clock);
    }

    public static synchronized TokenService getInstance() {
        if (instance == null) {
            AppConfig conf = new AppConfig();
            String configured = conf.getJwtSecret();
            byte[] secret;
            if (configured == null || configured.isBlank()) {
                logger.warning("JWT_SECRET is not set; using a random secret, so tokens end with this run");
                secret = new byte[32];
                new SecureRandom().nextBytes(secret);
            } else {
                secret = configured.getBytes(StandardCharsets.UTF_8);
            }
            instance = new TokenService(secret, Duration.ofMinutes(conf.getTokenTtlMinutes()), Clock.systemUTC());
        }
        return instance;
    }

    public String issue(String username, String role) {
        Instant now = clock.instant();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(username)
                .withClaim(ROLE_CLAIM, role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(ttl)))
                .sign(algorithm);
    }

    public long ttlSeconds() {
        return ttl.getSeconds();
    }

    /** The caller, or empty when the token is missing, forged or expired. */
    public Optional<Caller> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            DecodedJWT jwt = verifier.verify(token);
            if (jwt.getExpiresAtAsInstant() == null) {
                return Optional.empty();
            }
            String role = jwt.getClaim(ROLE_CLAIM).asString();
            if (jwt.getSubject() == null || role == null) {
                return Optional.empty();
            }
            return Optional.of(new Caller(jwt.getSubject(), role));
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    public static final class Caller {
        private final String username;
        private final String role;

        public Caller(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
