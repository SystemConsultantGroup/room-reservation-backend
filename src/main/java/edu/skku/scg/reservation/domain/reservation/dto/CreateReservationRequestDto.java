package edu.skku.scg.reservation.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateReservationRequestDto (
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
        String purpose
) {
}
