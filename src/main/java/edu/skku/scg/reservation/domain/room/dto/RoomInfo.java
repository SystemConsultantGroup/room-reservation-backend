package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummary;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;
import lombok.Builder;

import java.util.List;

@Builder
public record RoomInfo(
        Long id,
        String name,
        Integer minAttendeeCount,
        Integer maxAttendeeCount,
        String roomNumber,
        RoomAccessPolicy accessPolicy,
        Integer minUsageMinutes,
        Integer maxUsageMinutes,
        List<MajorSummary> majors
) {
}
