package com.softy.be.auth.dto;

public record TeacherSignupRequest(
        String teacherName,
        String schoolName,
        Integer grade,
        Integer classNumber
) {
}
