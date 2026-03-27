package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummaryDto;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import lombok.Builder;

import java.util.List;

@Builder
public record RoomInfoDto(
        Long id,
        String name,
        Integer capacity,
        String roomNumber,
        RoomAccessPolicy accessPolicy,
        Integer maxBookingMinutes,
        List<MajorSummaryDto> majors
) {
}
