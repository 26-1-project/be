package com.softy.be.service;

public record TeacherSignupResult(
        Long userId,
        String role,
        String classCode
) {
}
