package com.softy.be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "class_code")
@Getter
@NoArgsConstructor
public class ClassCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    public static ClassCode create(String code, Classroom classroom) {
        ClassCode classCode = new ClassCode();
        classCode.code = code;
        classCode.classroom = classroom;
        return classCode;
    }
}
