package com.softy.be.dto;

import java.time.LocalDate;

public record ParentSignupRequest(
        String parentName,
        String studentName,
        LocalDate studentBirthday,
        String studentGender,
        String classCode
) {
}
