package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoomCreateRequestDto(
        @NotBlank
        String name,
        Integer capacity,
        String roomNumber,

        @NotNull
        RoomAccessPolicy accessPolicy,

        @NotEmpty
        List<Long> majorIds
) {}
