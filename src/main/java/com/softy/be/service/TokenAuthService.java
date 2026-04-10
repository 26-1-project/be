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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Bearer 토큰이 필요합니다");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 토큰이 비어있습니다");
        }

        try {
            return jwtService.extractUserId(token);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다", e);
        }
    }
}
