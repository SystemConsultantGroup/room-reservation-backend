package edu.skku.scg.reservation.domain.user.entity;

import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;
import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users_management_unit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserManagementUnit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private ManagementUnit managementUnit;
}