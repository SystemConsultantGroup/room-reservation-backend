package edu.skku.scg.reservation.domain.room.dto;

import lombok.Builder;

@Builder
public record RoomSummary(
        Long id,
        String name
) {
}
