package com.softy.be.admin.dto;

public record AdminRegisterRequest(
        String name,
        String loginId,
        String password
) {
}
