package com.softy.be.controller;

import com.softy.be.dto.ApiResponse;
import com.softy.be.dto.AuthTokenResponse;
import com.softy.be.dto.ClassCodeData;
import com.softy.be.dto.ParentSignupRequest;
import com.softy.be.dto.SignupUserData;
import com.softy.be.dto.TeacherSignupRequest;
import com.softy.be.service.AuthResult;
import com.softy.be.service.AuthService;
import com.softy.be.service.ClassCodeCreateResult;
import com.softy.be.service.KakaoOAuthClient;
import com.softy.be.service.ParentSignupResult;
import com.softy.be.service.TeacherSignupResult;
import com.softy.be.service.TokenAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String OAUTH_STATE_KEY = "KAKAO_OAUTH_STATE";

    private final KakaoOAuthClient kakaoOAuthClient;
    private final AuthService authService;
    private final TokenAuthService tokenAuthService;

    @GetMapping("/kakao/login")
    public ResponseEntity<Void> redirectToKakao(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute(OAUTH_STATE_KEY, state);

        URI authorizeUri = kakaoOAuthClient.buildAuthorizeUri(state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(authorizeUri)
                .build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<AuthTokenResponse> kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session
    ) {
        if (error != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uCE74\uCE74\uC624 \uB85C\uADF8\uC778\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4: " + error);
        }
        if (code == null || state == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uC778\uAC00 \uCF54\uB4DC \uB610\uB294 \uC0C1\uD0DC\uAC12\uC774 \uB204\uB77D\uB418\uC5C8\uC2B5\uB2C8\uB2E4");
        }

        Object storedState = session.getAttribute(OAUTH_STATE_KEY);
        session.removeAttribute(OAUTH_STATE_KEY);

        if (storedState == null || !state.equals(storedState.toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uC720\uD6A8\uD558\uC9C0 \uC54A\uC740 \uC0C1\uD0DC\uAC12\uC785\uB2C8\uB2E4");
        }

        AuthResult result = authService.loginWithKakaoCode(code);

        return ResponseEntity.ok(new AuthTokenResponse(
                result.accessToken(),
                "Bearer",
                result.userId(),
                result.name(),
                result.role(),
                result.provider(),
                result.registrationRequired()
        ));
    }

    @PostMapping("/teachers/signup")
    public ResponseEntity<ApiResponse<SignupUserData>> signupTeacher(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody TeacherSignupRequest request
    ) {
        Long userId = tokenAuthService.extractUserIdFromAuthorization(authorization);
        TeacherSignupResult result = authService.signupTeacher(userId, request);

        ApiResponse<SignupUserData> response = ApiResponse.of(
                true,
                201,
                "\uAD50\uC0AC \uD68C\uC6D0\uAC00\uC785\uC774 \uC644\uB8CC\uB418\uC5C8\uC2B5\uB2C8\uB2E4.",
                new SignupUserData(result.userId(), result.role())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/parents/signup")
    public ResponseEntity<ApiResponse<SignupUserData>> signupParent(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody ParentSignupRequest request
    ) {
        Long userId = tokenAuthService.extractUserIdFromAuthorization(authorization);
        ParentSignupResult result = authService.signupParent(userId, request);

        ApiResponse<SignupUserData> response = ApiResponse.of(
                true,
                201,
                "\uD559\uBD80\uBAA8 \uD68C\uC6D0\uAC00\uC785\uC774 \uC644\uB8CC\uB418\uC5C8\uC2B5\uB2C8\uB2E4.",
                new SignupUserData(result.userId(), result.role())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/teachers/classcode")
    public ResponseEntity<ApiResponse<ClassCodeData>> createClassCode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        Long userId = tokenAuthService.extractUserIdFromAuthorization(authorization);
        ClassCodeCreateResult result = authService.createTeacherClassCode(userId);

        ApiResponse<ClassCodeData> response = ApiResponse.of(
                true,
                201,
                "\uD074\uB798\uC2A4\uCF54\uB4DC \uBC1C\uAE09\uC774 \uC644\uB8CC\uB418\uC5C8\uC2B5\uB2C8\uB2E4.",
                new ClassCodeData(result.classCode())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
