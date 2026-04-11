package com.softy.be.admin.dto;

public record AdminRegisterData(
        Long userId,
        String role,
        String loginId
) {
}
