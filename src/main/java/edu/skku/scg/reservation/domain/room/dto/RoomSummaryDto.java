package edu.skku.scg.reservation.domain.room.dto;

import lombok.Builder;

@Builder
public record RoomSummaryDto(
        Long id,
        String name
) {
}
