package edu.skku.scg.reservation.domain.reservation.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.reservation.dto.CreateReservationRequest;
import edu.skku.scg.reservation.domain.reservation.dto.ReservationList;
import edu.skku.scg.reservation.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "예약 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성")
    @PostMapping
    public void createReservation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateReservationRequest dto) {

        reservationService.reserveRoom(userPrincipal.getId(), dto);
    }

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/me")
    public ReservationList getMyReservations(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return reservationService.getMyReservations(userPrincipal.getId(), LocalDateTime.now());
    }

    @Operation(summary = "예약 취소")
    @DeleteMapping("/{reservationId}")
    public void deleteReservation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long reservationId) {

        reservationService.deleteReservation(userPrincipal.getId(), reservationId, userPrincipal.getManagingUnitIds());
    }
}