package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.Room;

public record RoomSummary(
        Long id,
        String name,
        Boolean canReserve
) {
    public static RoomSummary from(Room room, boolean canReserve) {
        return new RoomSummary(room.getId(), room.getName(), canReserve);
    }
}
