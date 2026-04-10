package com.softy.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserMeData(
        String role,
        String name,
        Integer grade,
        @JsonProperty("class") Integer classNumber
) {
}
