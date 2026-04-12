package edu.skku.scg.reservation.domain.reservation.dto;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;

import java.util.List;

public record ReservationList(
        List<ReservationDetail> reservations
) {
    public static ReservationList from(List<Reservation> reservations) {
        return new ReservationList(reservations.stream().map(ReservationDetail::from).toList());
    }
}
