package com.softy.be.repository.school;

import com.softy.be.domain.school.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findFirstByTeacherIdOrderByIdDesc(Long teacherId);
}
