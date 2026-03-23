package edu.skku.scg.reservation.domain.room.dto;

import edu.skku.scg.reservation.domain.room.entity.RoomAccessPolicy;

import java.util.List;

public record RoomCreateRequestDto(
        String name,
        Integer capacity,
        String roomNumber,
        RoomAccessPolicy accessPolicy,
        List<Long> majorIds
) {}
