package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomOperatingHour;

import java.time.LocalTime;
import java.util.List;

public record DailyRoomScheduleResponse(
        Long id,
        String name,
        String roomNumber,
        LocalTime openTime,
        LocalTime closeTime,
        List<ReservationDetail> reservations
) {
    public static DailyRoomScheduleResponse from(Room room, RoomOperatingHour todayHour, List<Reservation> reservations) {
        return new DailyRoomScheduleResponse(
                room.getId(),
                room.getName(),
                room.getRoomNumber(),
                todayHour != null ? todayHour.getOpenTime() : null,
                todayHour != null ? todayHour.getCloseTime() : null,
                reservations.stream().map(ReservationDetail::from).toList()
        );
    }
}
