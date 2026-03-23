package edu.skku.scg.reservation.domain.auth.dto;

import edu.skku.scg.reservation.domain.user.entity.MajorType;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OnboardingRequestDto(
        String studentId,

        @NotNull
        UserType userType,

        @Valid
        List<MajorRequest> majors
) {
        public record MajorRequest(
                @NotNull
                Long majorId,

                MajorType type
        ) {
        }
}