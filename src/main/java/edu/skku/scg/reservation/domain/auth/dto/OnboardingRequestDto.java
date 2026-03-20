package edu.skku.scg.reservation.domain.auth.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import jakarta.validation.constraints.NotNull;

public record OnboardingRequestDto(
        String studentId,

        @NotNull
        UserType type
) {
}