package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummaryDto;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetailDto;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import lombok.Builder;

import java.util.List;

@Builder
public record RoomScheduleResponseDto (
        Long id,
        String name,
        Integer capacity,
        RoomAccessPolicy accessPolicy,
        List<MajorSummaryDto> majors,
        List<ReservationDetailDto> reservations
) {}
