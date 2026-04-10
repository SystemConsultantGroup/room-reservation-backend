package edu.skku.scg.reservation.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull
        Long roomId,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime,

        @NotNull
        @Positive
        Integer attendeeCount,

        @NotBlank
        @Size(max = 100)
        String purpose
) {}
