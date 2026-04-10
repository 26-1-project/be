package com.softy.be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TokenAuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public Long extractUserIdFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Bearer \uD1A0\uD070\uC774 \uD544\uC694\uD569\uB2C8\uB2E4");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT \uD1A0\uD070\uC774 \uBE44\uC5B4\uC788\uC2B5\uB2C8\uB2E4");
        }

        try {
            return jwtService.extractUserId(token);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "\uC720\uD6A8\uD558\uC9C0 \uC54A\uC740 JWT \uD1A0\uD070\uC785\uB2C8\uB2E4", e);
        }
    }
}
