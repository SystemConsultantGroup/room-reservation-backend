package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetailDto;
import lombok.Builder;

import java.util.List;

@Builder
public record WeeklyRoomScheduleResponseDto(
        Long id,
        List<ReservationDetailDto> reservations
) {}
