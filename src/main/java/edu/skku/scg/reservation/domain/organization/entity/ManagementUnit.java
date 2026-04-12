package edu.skku.scg.reservation.domain.organization.entity;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(length = 100)
    private String noticeTitle;

    @Column(columnDefinition = "TEXT")
    private String noticeContent;

    @Column(columnDefinition = "TEXT")
    private String approvalMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType defaultUserType;

    public void updateNotice(String title, String content) {
        this.noticeTitle = title;
        this.noticeContent = content;
    }
}
