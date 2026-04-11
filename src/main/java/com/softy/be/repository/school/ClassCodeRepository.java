package com.softy.be.repository.school;

import com.softy.be.domain.school.ClassCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassCodeRepository extends JpaRepository<ClassCode, Long> {
    boolean existsByCode(String code);
    Optional<ClassCode> findFirstByCodeOrderByIdDesc(String code);
}
