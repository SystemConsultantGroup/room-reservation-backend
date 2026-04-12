package edu.skku.scg.reservation.domain.user.entity;

import edu.skku.scg.reservation.domain.organization.entity.Major;
import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users_major")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMajor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Enumerated(EnumType.STRING)
    private MajorType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Builder
    public UserMajor(Long id, User user, Major major, MajorType type) {
        this.id = id;
        this.user = user;
        this.major = major;
        this.type = type;
        this.status = RegistrationStatus.PENDING;
    }

    public void approve() {
        this.status = RegistrationStatus.APPROVED;
    }

    public void reject() {
        this.status = RegistrationStatus.REJECTED;
    }
}
