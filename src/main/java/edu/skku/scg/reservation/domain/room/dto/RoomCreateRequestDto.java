package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RoomCreateRequestDto(
        @NotBlank
        String name,

        @Positive
        Integer capacity,
        String roomNumber,

        @NotNull
        RoomAccessPolicy accessPolicy,

        @Positive
        Integer maxBookingMinutes,

        @NotEmpty
        List<Long> majorIds
) {}
