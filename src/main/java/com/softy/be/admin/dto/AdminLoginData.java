package com.softy.be.admin.dto;

public record AdminLoginData(
        String accessToken,
        String refreshToken
) {
}
