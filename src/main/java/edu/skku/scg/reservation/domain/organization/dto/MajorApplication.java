package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.entity.UserMajor;
import edu.skku.scg.reservation.domain.user.entity.MajorType;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;

import java.time.LocalDateTime;

public record MajorApplication(
        Long id,
        MajorSummary major,
        MajorType type,
        RegistrationStatus status,
        LocalDateTime createdAt
) {
    public static MajorApplication from(UserMajor userMajor) {
        return new MajorApplication(
                userMajor.getId(),
                MajorSummary.from(userMajor.getMajor()),
                userMajor.getType(),
                userMajor.getStatus(),
                userMajor.getCreatedAt()
        );
    }
}
