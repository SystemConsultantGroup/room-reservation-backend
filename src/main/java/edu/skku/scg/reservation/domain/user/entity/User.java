package edu.skku.scg.reservation.domain.user.entity;

import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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
    private final Set<CollegeMembership> collegeMemberships = new HashSet<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<CollegeAdmin> collegeAdmins = new HashSet<>();

    public User(String email, String name, String studentId, String googleId) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.googleId = googleId;
    }
}