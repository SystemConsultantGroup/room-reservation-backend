package edu.skku.scg.reservation.domain.room.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomSummaryList(
        List<RoomSummary> rooms
) {
}
