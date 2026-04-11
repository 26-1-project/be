package com.softy.be.admin.service;

public record AdminRegisterResult(
        Long userId,
        String role,
        String loginId
) {
}
