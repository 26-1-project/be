package com.softy.be.controller;

import com.softy.be.dto.AdminLoginData;
import com.softy.be.dto.AdminLoginRequest;
import com.softy.be.dto.AdminRegisterData;
import com.softy.be.dto.AdminRegisterRequest;
import com.softy.be.dto.ApiResponse;
import com.softy.be.service.AdminAuthService;
import com.softy.be.service.AdminLoginResult;
import com.softy.be.service.AdminRegisterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private static final String PROVISION_HEADER = "X-Admin-Provision-Key";

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginData>> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResult result = adminAuthService.login(request);

        ApiResponse<AdminLoginData> response = ApiResponse.of(
                true,
                200,
                "\uAD00\uB9AC\uC790 \uB85C\uADF8\uC778\uC5D0 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.",
                new AdminLoginData(result.accessToken(), result.refreshToken())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AdminRegisterData>> register(
            @RequestHeader(value = PROVISION_HEADER, required = false) String provisionKey,
            @RequestBody AdminRegisterRequest request
    ) {
        AdminRegisterResult result = adminAuthService.register(request, provisionKey);

        ApiResponse<AdminRegisterData> response = ApiResponse.of(
                true,
                201,
                "\uAD00\uB9AC\uC790 \uACC4\uC815\uC774 \uC0DD\uC131\uB418\uC5C8\uC2B5\uB2C8\uB2E4.",
                new AdminRegisterData(result.userId(), result.role(), result.loginId())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
