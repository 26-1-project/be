package com.softy.be.domain.school;

import com.softy.be.domain.common.BaseEntity;
import com.softy.be.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parent_student")
@Getter
@NoArgsConstructor
public class ParentStudent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public static ParentStudent create(User parent, Student student) {
        ParentStudent mapping = new ParentStudent();
        mapping.parent = parent;
        mapping.student = student;
        return mapping;
    }
}
