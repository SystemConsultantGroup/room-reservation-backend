package edu.skku.scg.reservation.domain.organization.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MajorApplicationRequest(
        @NotEmpty
        @Valid
        List<MajorRequest> majors
) {}
