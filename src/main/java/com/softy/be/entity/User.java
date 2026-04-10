package com.softy.be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "login_id")
    private String loginId;

    private String pw;

    private String role;

    public static User createForKakao(String name) {
        User user = new User();
        user.name = name;
        user.role = "UNASSIGNED";
        return user;
    }

    public static User createAdmin(String name, String loginId, String encodedPassword) {
        User user = new User();
        user.name = name;
        user.loginId = loginId;
        user.pw = encodedPassword;
        user.role = "ADMIN";
        return user;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void completeTeacherSignup(String name) {
        this.name = name;
        this.role = "TEACHER";
    }

    public void completeParentSignup(String name) {
        this.name = name;
        this.role = "PARENT";
    }

    public void withdraw() {
        this.name = "\uD0C8\uD1F4\uD68C\uC6D0_" + this.id;
        this.loginId = null;
        this.pw = null;
        this.role = "WITHDRAWN";
    }
}
