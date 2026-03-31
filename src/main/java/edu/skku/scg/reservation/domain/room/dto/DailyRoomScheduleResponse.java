package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummary;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationDetail;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Builder
public record DailyRoomScheduleResponse(
        Long id,
        String name,
        Integer capacity,
        RoomAccessPolicy accessPolicy,
        LocalTime openTime,
        LocalTime closeTime,
        List<MajorSummary> majors,
        List<ReservationDetail> reservations
) {}
