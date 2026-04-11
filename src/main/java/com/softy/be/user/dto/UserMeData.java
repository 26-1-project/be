package com.softy.be.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserMeData(
        String role,
        String name,
        Integer grade,
        @JsonProperty("class") Integer classNumber
) {
}
