package edu.skku.scg.reservation.domain.reservation.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ReservationList(
        List<ReservationDetail> reservations
) {}