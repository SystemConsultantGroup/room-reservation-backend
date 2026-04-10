package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoomCreateRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        @Positive
        Integer minAttendeeCount,

        @NotNull
        @Positive
        Integer maxAttendeeCount,

        @NotBlank
        @Size(max = 50)
        String roomNumber,

        @NotNull
        RoomAccessPolicy accessPolicy,

        @NotNull
        @Positive
        Integer minUsageMinutes,

        @NotNull
        @Positive
        Integer maxUsageMinutes,

        @NotEmpty
        List<Long> majorIds,

        @NotEmpty
        @Valid
        List<OperatingHoursDetail> operatingHours
) {}
