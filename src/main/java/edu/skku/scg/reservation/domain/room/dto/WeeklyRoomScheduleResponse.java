package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;

import java.util.List;

public record WeeklyRoomScheduleResponse(
        Long id,
        List<ReservationDetail> reservations
) {
    public static WeeklyRoomScheduleResponse from(Long roomId, List<Reservation> reservations) {
        return new WeeklyRoomScheduleResponse(
                roomId,
                reservations.stream().map(ReservationDetail::from).toList()
        );
    }
}
