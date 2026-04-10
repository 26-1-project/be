package com.softy.be.dto;

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
