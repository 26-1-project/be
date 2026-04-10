package com.softy.be.dto;

public record AdminRegisterData(
        Long userId,
        String role,
        String loginId
) {
}
