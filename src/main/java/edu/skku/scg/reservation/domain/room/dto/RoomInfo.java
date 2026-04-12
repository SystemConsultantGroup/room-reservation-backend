package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummary;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;

import java.util.List;

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
    public static RoomInfo from(Room room) {
        List<MajorSummary> majors = room.getMajorRooms().stream()
                .map(mr -> MajorSummary.from(mr.getMajor()))
                .toList();
        return new RoomInfo(
                room.getId(),
                room.getName(),
                room.getMinAttendeeCount(),
                room.getMaxAttendeeCount(),
                room.getRoomNumber(),
                room.getAccessPolicy(),
                room.getMinUsageMinutes(),
                room.getMaxUsageMinutes(),
                majors
        );
    }
}
