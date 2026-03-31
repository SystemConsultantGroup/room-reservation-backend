package edu.skku.scg.reservation.domain.reservation.dto;

import edu.skku.scg.reservation.domain.user.dto.UserSummary;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReservationDetail(
        Long id,
        UserSummary user,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer attendeeCount,
        String purpose
) {}