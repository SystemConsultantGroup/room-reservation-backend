package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.entity.MajorType;
import jakarta.validation.constraints.NotNull;

public record MajorRequest(
        @NotNull
        Long id,
        MajorType type
) {}