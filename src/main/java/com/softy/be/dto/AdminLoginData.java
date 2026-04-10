package com.softy.be.dto;

public record AdminLoginData(
        String accessToken,
        String refreshToken
) {
}
