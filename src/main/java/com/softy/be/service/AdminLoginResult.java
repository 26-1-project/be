package com.softy.be.service;

public record AdminLoginResult(
        String accessToken,
        String refreshToken
) {
}
