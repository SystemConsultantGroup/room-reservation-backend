package edu.skku.scg.reservation.domain.user.entity;

import edu.skku.scg.reservation.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String studentId;

    @Column(unique = true, nullable = false)
    private String googleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CollegeMembership> collegeMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CollegeAdmin> collegeAdmins = new ArrayList<>();

    public User(String email, String name, String studentId, String googleId) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.googleId = googleId;
    }
}