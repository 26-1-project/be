package com.softy.be.service;

public record UserMeResult(
        String role,
        String name,
        Integer grade,
        Integer classNumber
) {
}
