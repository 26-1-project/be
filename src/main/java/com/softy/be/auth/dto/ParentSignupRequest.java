package com.softy.be.auth.dto;

import java.time.LocalDate;

public record ParentSignupRequest(
        String parentName,
        String studentName,
        LocalDate studentBirthday,
        String studentGender,
        String classCode
) {
}
