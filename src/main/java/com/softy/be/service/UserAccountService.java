package com.softy.be.service;

import com.softy.be.entity.Classroom;
import com.softy.be.entity.ParentStudent;
import com.softy.be.entity.User;
import com.softy.be.repository.ClassroomRepository;
import com.softy.be.repository.ParentStudentRepository;
import com.softy.be.repository.SocialAccountRepository;
import com.softy.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final ClassroomRepository classroomRepository;
    private final ParentStudentRepository parentStudentRepository;

    @Transactional(readOnly = true)
    public UserMeResult getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC0AC\uC6A9\uC790\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        Integer grade = null;
        Integer classNumber = null;

        if ("TEACHER".equalsIgnoreCase(user.getRole())) {
            Classroom classroom = classroomRepository.findFirstByTeacherIdOrderByIdDesc(userId).orElse(null);
            if (classroom != null) {
                grade = classroom.getGrade();
                classNumber = classroom.getClassNumber();
            }
        } else if ("PARENT".equalsIgnoreCase(user.getRole())) {
            ParentStudent mapping = parentStudentRepository.findFirstByParentIdOrderByIdDesc(userId).orElse(null);
            if (mapping != null && mapping.getStudent() != null && mapping.getStudent().getClassroom() != null) {
                grade = mapping.getStudent().getClassroom().getGrade();
                classNumber = mapping.getStudent().getClassroom().getClassNumber();
            }
        }

        return new UserMeResult(user.getRole(), user.getName(), grade, classNumber);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "\uC0AC\uC6A9\uC790\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4"));

        if ("WITHDRAWN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "\uC774\uBBF8 \uD0C8\uD1F4\uD55C \uACC4\uC815\uC785\uB2C8\uB2E4");
        }

        socialAccountRepository.deleteAllByUserId(userId);
        user.withdraw();
    }
}
