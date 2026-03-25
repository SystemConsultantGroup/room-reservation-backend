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
public class ManagementUnit extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String noticeTitle;

    @Column(columnDefinition = "TEXT")
    private String noticeContent;

    @Builder
    public ManagementUnit(String name, String noticeTitle, String noticeContent) {
        this.name = name;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
    }
}