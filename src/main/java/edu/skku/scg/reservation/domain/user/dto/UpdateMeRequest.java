package edu.skku.scg.reservation.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {}
