package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.entity.MajorType;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MajorApplication(
        Long id,
        MajorSummary major,
        MajorType type,
        RegistrationStatus status,
        LocalDateTime createdAt
) {}
