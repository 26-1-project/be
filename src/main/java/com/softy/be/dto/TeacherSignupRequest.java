package com.softy.be.dto;

public record TeacherSignupRequest(
        String teacherName,
        String schoolName,
        Integer grade,
        Integer classNumber
) {
}
