package com.softy.be.controller;

import com.softy.be.dto.ApiResponse;
import com.softy.be.dto.UserMeData;
import com.softy.be.service.TokenAuthService;
import com.softy.be.service.UserMeResult;
import com.softy.be.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final TokenAuthService tokenAuthService;
    private final UserAccountService userAccountService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeData>> me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        Long userId = tokenAuthService.extractUserIdFromAuthorization(authorization);
        UserMeResult result = userAccountService.getMe(userId);

        ApiResponse<UserMeData> response = ApiResponse.of(
                true,
                200,
                "\uD604\uC7AC \uC0AC\uC6A9\uC790 \uC815\uBCF4 \uC870\uD68C\uC5D0 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.",
                new UserMeData(result.role(), result.name(), result.grade(), result.classNumber())
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Object>> withdraw(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        Long userId = tokenAuthService.extractUserIdFromAuthorization(authorization);
        userAccountService.withdraw(userId);

        ApiResponse<Object> response = ApiResponse.of(
                true,
                200,
                "\uD68C\uC6D0 \uD0C8\uD1F4\uAC00 \uC644\uB8CC\uB418\uC5C8\uC2B5\uB2C8\uB2E4.",
                null
        );

        return ResponseEntity.ok(response);
    }
}
