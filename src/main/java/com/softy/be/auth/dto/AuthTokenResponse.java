package com.softy.be.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String name,
        String role,
        String provider,
        boolean registrationRequired
) {
}
