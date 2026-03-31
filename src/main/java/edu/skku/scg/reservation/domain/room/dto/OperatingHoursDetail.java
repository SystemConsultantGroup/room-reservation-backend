package edu.skku.scg.reservation.domain.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record OperatingHoursDetail(
        @NotNull
        DayOfWeek dayOfWeek,

        @NotNull
        LocalTime openTime,

        @NotNull
        LocalTime closeTime
) {}
