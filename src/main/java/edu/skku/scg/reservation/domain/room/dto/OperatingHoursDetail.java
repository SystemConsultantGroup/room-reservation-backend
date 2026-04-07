package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OperatingHoursDetail(
        @NotNull
        DayOfWeek dayOfWeek,

        @NotNull
        LocalTime openTime,

        @NotNull
        LocalTime closeTime
) {
    public static OperatingHoursDetail from(RoomOperatingHour operatingHour) {
        return new OperatingHoursDetail(
                operatingHour.getDayOfWeek(),
                operatingHour.getOpenTime(),
                operatingHour.getCloseTime()
        );
    }
}
