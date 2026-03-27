package edu.skku.scg.reservation.domain.room.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomSummaryListDto(
        List<RoomSummaryDto> content
) {
}
