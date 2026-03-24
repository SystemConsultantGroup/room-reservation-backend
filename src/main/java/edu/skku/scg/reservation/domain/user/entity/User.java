package edu.skku.scg.reservation.domain.user.entity;

import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private UserType type;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMajor> userMajors = new ArrayList<>();

    @Builder
    public User(String email, String name, String googleId) {
        this.email = email;
        this.name = name;
        this.googleId = googleId;
        this.type = UserType.GUEST;
    }

    public void completeOnboarding(UserType userType, String studentId) {
        if (this.type != UserType.GUEST) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_USER);
        }

        if (userType == UserType.STUDENT) {
            if (studentId == null || !studentId.matches("^\\d{10}$")) {
                throw new BusinessException(ErrorCode.INVALID_STUDENT_ID_FORMAT);
            }
        } else if (userType == UserType.FACULTY) {
            if (studentId != null && !studentId.isBlank()) {
                throw new BusinessException(ErrorCode.STUDENT_ID_NOT_ALLOWED);
            }
            studentId = null;
        } else {
            throw new BusinessException(ErrorCode.INVALID_USER_TYPE);
        }

        this.type = userType;
        this.studentId = studentId;
    }
}