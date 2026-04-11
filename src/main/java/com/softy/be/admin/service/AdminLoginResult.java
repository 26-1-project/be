package com.softy.be.admin.service;

public record AdminLoginResult(
        String accessToken,
        String refreshToken
) {
}
