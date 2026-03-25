package edu.skku.scg.reservation.domain.organization.entity;

import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Major extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "management_unit_id", nullable = false)
    private ManagementUnit managementUnit;

    @Builder
    public Major(String name, ManagementUnit managementUnit) {
        this.name = name;
        this.managementUnit = managementUnit;
    }
}