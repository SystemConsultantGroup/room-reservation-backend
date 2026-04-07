package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorSummary;
import edu.skku.scg.reservation.domain.room.entity.Room;
import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;

import java.util.List;

public record RoomResponse(
        Long id,
        String name,
        Integer minAttendeeCount,
        Integer maxAttendeeCount,
        String roomNumber,
        RoomAccessPolicy accessPolicy,
        Integer minUsageMinutes,
        Integer maxUsageMinutes,
        List<MajorSummary> majors,
        List<OperatingHoursDetail> operatingHours
) {
    public static RoomResponse from(Room room) {
        List<MajorSummary> majors = room.getMajorRooms().stream()
                .map(majorRoom -> MajorSummary.from(majorRoom.getMajor()))
                .toList();

        List<OperatingHoursDetail> operatingHours = room.getOperatingHours().stream()
                .map(OperatingHoursDetail::from)
                .toList();

        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getMinAttendeeCount(),
                room.getMaxAttendeeCount(),
                room.getRoomNumber(),
                room.getAccessPolicy(),
                room.getMinUsageMinutes(),
                room.getMaxUsageMinutes(),
                majors,
                operatingHours
        );
    }
}
