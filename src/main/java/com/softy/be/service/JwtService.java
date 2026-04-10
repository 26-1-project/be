package com.softy.be.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds:86400}") long accessExpirationSeconds,
            @Value("${jwt.refresh-expiration-seconds:1209600}") long refreshExpirationSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(sha256(secret));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String createAccessToken(Long userId, String name, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("name", name)
                .claim("role", role)
                .claim("tokenType", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public String createRefreshToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("tokenType", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public Long extractUserId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(subject);
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalStateException("\uC720\uD6A8\uD558\uC9C0 \uC54A\uC740 JWT \uD1A0\uD070\uC785\uB2C8\uB2E4", e);
        }
    }

    private byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 \uC54C\uACE0\uB9AC\uC998\uC744 \uC0AC\uC6A9\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4", e);
        }
    }
}
