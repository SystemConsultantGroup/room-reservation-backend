package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Builder
public record DailyRoomScheduleResponse(
        Long id,
        String name,
        String roomNumber,
        LocalTime openTime,
        LocalTime closeTime,
        List<ReservationDetail> reservations
) {}
