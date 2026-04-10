package com.softy.be.service;

public record AuthResult(
        String accessToken,
        Long userId,
        String name,
        String role,
        String provider,
        boolean registrationRequired
) {
}
