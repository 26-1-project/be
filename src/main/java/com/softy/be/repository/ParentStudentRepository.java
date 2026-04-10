package com.softy.be.repository;

import com.softy.be.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    Optional<ParentStudent> findFirstByParentIdOrderByIdDesc(Long parentId);
}
