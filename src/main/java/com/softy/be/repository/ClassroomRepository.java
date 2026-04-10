package com.softy.be.repository;

import com.softy.be.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findFirstByTeacherIdOrderByIdDesc(Long teacherId);
}
