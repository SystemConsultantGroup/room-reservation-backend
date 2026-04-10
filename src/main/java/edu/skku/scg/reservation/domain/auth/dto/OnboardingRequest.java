package edu.skku.scg.reservation.domain.auth.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorRequest;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        String studentId,

        @NotNull
        UserType userType,

        @NotNull
        @Valid
        List<MajorRequest> majors
) {}
