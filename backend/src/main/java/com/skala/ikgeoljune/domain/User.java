package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** AUTH-001: 비밀번호 원문은 저장하지 않고 BCrypt 해시만 저장한다. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    private User(String email, String passwordHash, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
    }

    public static User create(String email, String passwordHash, String name) {
        return new User(email, passwordHash, name);
    }

    /** USER-002 내 이름 수정 */
    public void changeName(String name) {
        this.name = name;
        touch();
    }
}
