package com.softy.be.admin.dto;

public record AdminLoginRequest(
        String loginId,
        String password
) {
}
