package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.Room;

import java.util.List;

public record RoomSummaryList(
        List<RoomSummary> rooms
) {
    public static RoomSummaryList from(List<Room> rooms, java.util.function.Predicate<Room> canReserve) {
        return new RoomSummaryList(
                rooms.stream()
                        .map(room -> RoomSummary.from(room, canReserve.test(room)))
                        .toList()
        );
    }
}
