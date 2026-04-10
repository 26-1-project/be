package com.softy.be.service;

import com.softy.be.dto.ParentSignupRequest;
import com.softy.be.dto.TeacherSignupRequest;
import com.softy.be.entity.ClassCode;
import com.softy.be.entity.Classroom;
import com.softy.be.entity.ParentStudent;
import com.softy.be.entity.School;
import com.softy.be.entity.SocialAccount;
import com.softy.be.entity.Student;
import com.softy.be.entity.User;
import com.softy.be.repository.ClassCodeRepository;
import com.softy.be.repository.ClassroomRepository;
import com.softy.be.repository.ParentStudentRepository;
import com.softy.be.repository.SchoolRepository;
import com.softy.be.repository.SocialAccountRepository;
import com.softy.be.repository.StudentRepository;
import com.softy.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String KAKAO_PROVIDER = "KAKAO";
    private static final String ROLE_UNASSIGNED = "UNASSIGNED";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_PARENT = "PARENT";
    private static final String LEGACY_ROLE_USER = "USER";
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SchoolRepository schoolRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassCodeRepository classCodeRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResult loginWithKakaoCode(String code) {
        String kakaoAccessToken = kakaoOAuthClient.exchangeCodeForAccessToken(code);
        KakaoUserProfile profile = kakaoOAuthClient.getUserProfile(kakaoAccessToken);

        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(KAKAO_PROVIDER, profile.providerUserId())
                .orElseGet(() -> createKakaoAccount(profile));

        User user = socialAccount.getUser();
        if (!Objects.equals(user.getName(), profile.nickname())) {
            user.updateName(profile.nickname());
        }

        String accessToken = jwtService.createAccessToken(user.getId(), user.getName(), user.getRole());
        boolean registrationRequired = isRegistrationRequired(user.getRole());
        return new AuthResult(accessToken, user.getId(), user.getName(), user.getRole(), KAKAO_PROVIDER, registrationRequired);
    }

    @Transactional
    public TeacherSignupResult signupTeacher(Long authenticatedUserId, TeacherSignupRequest request) {
        validateTeacherSignupRequest(request);

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC0AC\uC6A9\uC790\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        if (!isRegistrationRequired(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "\uC774\uBBF8 \uD68C\uC6D0\uAC00\uC785\uC774 \uC644\uB8CC\uB41C \uC0AC\uC6A9\uC790\uC785\uB2C8\uB2E4");
        }

        School school = schoolRepository.findByName(request.schoolName().trim())
                .orElseGet(() -> schoolRepository.save(School.create(request.schoolName().trim())));

        Classroom classroom = classroomRepository.save(
                Classroom.create(request.grade(), request.classNumber(), school, user)
        );

        String code = generateUniqueClassCode();
        classCodeRepository.save(ClassCode.create(code, classroom));

        user.completeTeacherSignup(request.teacherName().trim());
        return new TeacherSignupResult(user.getId(), user.getRole(), code);
    }

    @Transactional
    public ClassCodeCreateResult createTeacherClassCode(Long authenticatedUserId) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC0AC\uC6A9\uC790\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        if (!ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "\uAD50\uC0AC \uACC4\uC815\uB9CC \uD074\uB798\uC2A4 \uCF54\uB4DC\uB97C \uC0DD\uC131\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4");
        }

        Classroom classroom = classroomRepository.findFirstByTeacherIdOrderByIdDesc(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC0DD\uC131\uD560 \uD559\uAE09 \uC815\uBCF4\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        String code = generateUniqueClassCode();
        classCodeRepository.save(ClassCode.create(code, classroom));
        return new ClassCodeCreateResult(code);
    }

    @Transactional
    public ParentSignupResult signupParent(Long authenticatedUserId, ParentSignupRequest request) {
        validateParentSignupRequest(request);

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC0AC\uC6A9\uC790\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        if (!isRegistrationRequired(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "\uC774\uBBF8 \uD68C\uC6D0\uAC00\uC785\uC774 \uC644\uB8CC\uB41C \uC0AC\uC6A9\uC790\uC785\uB2C8\uB2E4");
        }

        ClassCode classCode = classCodeRepository.findFirstByCodeOrderByIdDesc(request.classCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC720\uD6A8\uD55C \uD559\uAE09 \uCF54\uB4DC\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        Student student = studentRepository.save(
                Student.create(
                        request.studentName().trim(),
                        request.studentBirthday(),
                        request.studentGender().trim().toUpperCase(),
                        classCode.getClassroom()
                )
        );
        parentStudentRepository.save(ParentStudent.create(user, student));

        user.completeParentSignup(request.parentName().trim());
        return new ParentSignupResult(user.getId(), user.getRole());
    }

    private SocialAccount createKakaoAccount(KakaoUserProfile profile) {
        User user = User.createForKakao(profile.nickname());
        userRepository.save(user);

        SocialAccount socialAccount = SocialAccount.create(user, KAKAO_PROVIDER, profile.providerUserId());
        return socialAccountRepository.save(socialAccount);
    }

    private boolean isRegistrationRequired(String role) {
        return role == null || ROLE_UNASSIGNED.equalsIgnoreCase(role) || LEGACY_ROLE_USER.equalsIgnoreCase(role);
    }

    private void validateTeacherSignupRequest(TeacherSignupRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uC694\uCCAD \uBCF8\uBB38\uC774 \uD544\uC694\uD569\uB2C8\uB2E4");
        }
        if (isBlank(request.teacherName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uAD50\uC0AC \uC774\uB984\uC740 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (isBlank(request.schoolName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uAD50 \uC774\uB984\uC740 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (request.grade() == null || request.grade() < 1 || request.grade() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uB144\uC740 1~6 \uC0AC\uC774\uC5EC\uC57C \uD569\uB2C8\uB2E4");
        }
        if (request.classNumber() == null || request.classNumber() < 1 || request.classNumber() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uBC18 \uBC88\uD638\uB294 1~30 \uC0AC\uC774\uC5EC\uC57C \uD569\uB2C8\uB2E4");
        }
    }

    private void validateParentSignupRequest(ParentSignupRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uC694\uCCAD \uBCF8\uBB38\uC774 \uD544\uC694\uD569\uB2C8\uB2E4");
        }
        if (isBlank(request.parentName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uBD80\uBAA8 \uC774\uB984\uC740 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (isBlank(request.studentName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uC0DD \uC774\uB984\uC740 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (request.studentBirthday() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uC0DD \uC0DD\uC77C\uC740 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        if (isBlank(request.studentGender())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uC0DD \uC131\uBCC4\uB294 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
        String gender = request.studentGender().trim().toUpperCase();
        if (!"M".equals(gender) && !"F".equals(gender)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uC0DD \uC131\uBCC4\uB294 M \uB610\uB294 F\uB85C \uC785\uB825\uD574\uC57C \uD569\uB2C8\uB2E4");
        }
        if (isBlank(request.classCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "\uD559\uAE09 \uCF54\uB4DC\uB294 \uD544\uC218\uC785\uB2C8\uB2E4");
        }
    }

    private String generateUniqueClassCode() {
        for (int i = 0; i < 20; i++) {
            String candidate = randomCodeChunk(3) + "-" + randomCodeChunk(3);
            if (!classCodeRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "\uACE0\uC720\uD55C \uD559\uAE09 \uCF54\uB4DC\uB97C \uC0DD\uC131\uD558\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4");
    }

    private String randomCodeChunk(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            int index = RANDOM.nextInt(CODE_CHARS.length());
            builder.append(CODE_CHARS.charAt(index));
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
