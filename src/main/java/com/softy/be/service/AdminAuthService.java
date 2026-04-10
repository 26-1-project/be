package com.softy.be.service;

import com.softy.be.dto.AdminLoginRequest;
import com.softy.be.dto.AdminRegisterRequest;
import com.softy.be.entity.User;
import com.softy.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${admin.provision-key}")
    private String provisionKey;

    @Transactional(readOnly = true)
    public AdminLoginResult login(AdminLoginRequest request) {
        validateLoginRequest(request);

        User user = userRepository.findByLoginId(request.loginId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "\uB85C\uADF8\uC778 \uC815\uBCF4\uAC00 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4"));

        if (!ADMIN_ROLE.equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "\uAD00\uB9AC\uC790 \uACC4\uC815\uB9CC \uB85C\uADF8\uC778\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4");
        }

        if (!passwordEncoder.matches(request.password(), user.getPw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "\uB85C\uADF8\uC778 \uC815\uBCF4\uAC00 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4");
        }

        String accessToken = jwtService.createAccessToken(user.getId(), user.getName(), user.getRole());
        String refreshToken = jwtService.createRefreshToken(user.getId(), user.getRole());

        return new AdminLoginResult(accessToken, refreshToken);
    }

    @Transactional
    public AdminRegisterResult register(AdminRegisterRequest request, String requestProvisionKey) {
        validateProvisionKey(requestProvisionKey);
        validateRegisterRequest(request);

        String loginId = request.loginId().trim();
        if (userRepository.existsByLoginId(loginId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "\uC774\uBBF8 \uC0AC\uC6A9 \uC911\uC778 \uAD00\uB9AC\uC790 \uC544\uC774\uB514\uC785\uB2C8\uB2E4");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User admin = User.createAdmin(request.name().trim(), loginId, encodedPassword);
        User saved = userRepository.save(admin);

        return new AdminRegisterResult(saved.getId(), saved.getRole(), saved.getLoginId());
    }

    private void validateLoginRequest(AdminLoginRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uC694\uCCAD \uBCF8\uBB38\uC774 \uD544\uC694\uD569\uB2C8\uB2E4");
        }
        if (isBlank(request.loginId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uB85C\uADF8\uC778 \uC544\uC774\uB514\uB294 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uBE44\uBC00\uBC88\uD638\uB294 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
    }

    private void validateRegisterRequest(AdminRegisterRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uC694\uCCAD \uBCF8\uBB38\uC774 \uD544\uC694\uD569\uB2C8\uB2E4");
        }
        if (isBlank(request.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uAD00\uB9AC\uC790 \uC774\uB984\uC740 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (isBlank(request.loginId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uB85C\uADF8\uC778 \uC544\uC774\uB514\uB294 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (isBlank(request.password()) || request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uBE44\uBC00\uBC88\uD638\uB294 8\uC790 \uC774\uC0C1\uC774\uC5B4\uC57C \uD569\uB2C8\uB2E4");
        }
    }

    private void validateProvisionKey(String requestProvisionKey) {
        if (isBlank(provisionKey)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "\uAD00\uB9AC\uC790 \uC0DD\uC131 \uD0A4\uAC00 \uC124\uC815\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4");
        }
        if (isBlank(requestProvisionKey) || !provisionKey.equals(requestProvisionKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "\uAD00\uB9AC\uC790 \uC0DD\uC131 \uD0A4\uAC00 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
