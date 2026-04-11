package com.softy.be.auth.service;

public record TeacherSignupResult(
        Long userId,
        String role,
        String classCode
) {
}
