package com.softy.be.auth.controller;

import com.softy.be.global.api.ApiResponse;
import com.softy.be.auth.dto.AuthTokenResponse;
import com.softy.be.auth.dto.ClassCodeData;
import com.softy.be.auth.dto.KakaoLoginData;
import com.softy.be.auth.dto.KakaoLoginRequest;
import com.softy.be.auth.dto.ParentSignupRequest;
import com.softy.be.auth.dto.SignupUserData;
import com.softy.be.auth.dto.TeacherSignupRequest;
import com.softy.be.auth.service.AuthResult;
import com.softy.be.auth.service.AuthService;
import com.softy.be.auth.service.ClassCodeCreateResult;
import com.softy.be.auth.service.KakaoOAuthClient;
import com.softy.be.auth.service.KakaoLoginResult;
import com.softy.be.auth.service.ParentSignupResult;
import com.softy.be.auth.service.TeacherSignupResult;
import com.softy.be.auth.service.TokenAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${app.frontend-redirect-uri:}")
    private String frontendRedirectUri;

    @GetMapping("/kakao/login")
    public ResponseEntity<Void> redirectToKakao(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute(OAUTH_STATE_KEY, state);

        URI authorizeUri = kakaoOAuthClient.buildAuthorizeUri(state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(authorizeUri)
                .build();
    }

    @PostMapping("/kakao/token-login")
    public ResponseEntity<ApiResponse<KakaoLoginData>> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        KakaoLoginResult result = authService.loginWithKakaoAccessToken(
                request == null ? null : request.kakaoAccessToken()
        );

        ApiResponse<KakaoLoginData> response = ApiResponse.of(
                true,
                200,
                "로그인 성공했습니다.",
                new KakaoLoginData(result.accessToken(), result.refreshToken())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session
    ) {
        if (error != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카카오 로그인에 실패했습니다: " + error);
        }
        if (code == null || state == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인가 코드 또는 상태값이 누락되었습니다");
        }

        Object storedState = session.getAttribute(OAUTH_STATE_KEY);
        session.removeAttribute(OAUTH_STATE_KEY);

        if (storedState == null || !state.equals(storedState.toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다");
        }

        AuthResult result = authService.loginWithKakaoCode(code);

        AuthTokenResponse response = new AuthTokenResponse(
                result.accessToken(),
                "Bearer",
                result.userId(),
                result.name(),
                result.role(),
                result.provider(),
                result.registrationRequired()
        );

        if (isFrontendRedirectConfigured()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(buildFrontendSuccessUri(response))
                    .build();
        }

        return ResponseEntity.ok(response);
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
                "교사 회원가입이 완료되었습니다.",
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
                "Parent signup completed.",
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
                "클래스코드 발급이 완료되었습니다.",
                new ClassCodeData(result.classCode())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ResponseEntity<?> redirectWithError(String errorCode, String errorDescription) {
        if (isFrontendRedirectConfigured()) {
            URI redirectUri = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("error", errorCode)
                    .queryParam("errorDescription", errorDescription)
                    .build()
                    .encode()
                    .toUri();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(redirectUri)
                    .build();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorCode + ": " + errorDescription);
    }

    private URI buildFrontendSuccessUri(AuthTokenResponse response) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("accessToken", response.accessToken())
                .queryParam("tokenType", response.tokenType())
                .queryParam("userId", response.userId())
                .queryParam("name", response.name())
                .queryParam("role", response.role())
                .queryParam("provider", response.provider())
                .queryParam("registrationRequired", response.registrationRequired())
                .build()
                .encode()
                .toUri();
    }

    private boolean isFrontendRedirectConfigured() {
        return frontendRedirectUri != null && !frontendRedirectUri.isBlank();
    }
}
