package com.softy.be.user.service;

public record UserMeResult(
        String role,
        String name,
        Integer grade,
        Integer classNumber
) {
}
