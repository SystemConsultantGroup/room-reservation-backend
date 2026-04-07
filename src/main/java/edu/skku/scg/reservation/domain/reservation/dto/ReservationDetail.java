package edu.skku.scg.reservation.domain.reservation.dto;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.user.dto.UserSummary;

import java.time.LocalDateTime;

public record ReservationDetail(
        Long id,
        UserSummary user,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer attendeeCount,
        String purpose
) {
    public static ReservationDetail from(Reservation reservation) {
        return new ReservationDetail(
                reservation.getId(),
                UserSummary.from(reservation.getUser()),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getAttendeeCount(),
                reservation.getPurpose()
        );
    }
}
