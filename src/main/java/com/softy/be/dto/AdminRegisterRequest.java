package com.softy.be.dto;

public record AdminRegisterRequest(
        String name,
        String loginId,
        String password
) {
}
