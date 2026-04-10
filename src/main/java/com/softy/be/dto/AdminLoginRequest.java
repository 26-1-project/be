package com.softy.be.dto;

public record AdminLoginRequest(
        String loginId,
        String password
) {
}
