package com.softy.be.repository.school;

import com.softy.be.domain.school.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
