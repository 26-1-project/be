package com.softy.be.service;

public record AdminRegisterResult(
        Long userId,
        String role,
        String loginId
) {
}
