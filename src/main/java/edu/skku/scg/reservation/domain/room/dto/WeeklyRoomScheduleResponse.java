package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;
import lombok.Builder;

import java.util.List;

@Builder
public record WeeklyRoomScheduleResponse(
        Long id,
        List<ReservationDetail> reservations
) {}
