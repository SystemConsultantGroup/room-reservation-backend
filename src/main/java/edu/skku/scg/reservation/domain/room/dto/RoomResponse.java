package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummary;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import lombok.Builder;

import java.util.List;

@Builder
public record RoomResponse(
        Long id,
        String name,
        Integer capacity,
        String roomNumber,
        RoomAccessPolicy accessPolicy,
        Integer maxBookingMinutes,
        Boolean canReserve,
        List<MajorSummary> majors,
        List<OperatingHoursDetail> operatingHours
) {}
