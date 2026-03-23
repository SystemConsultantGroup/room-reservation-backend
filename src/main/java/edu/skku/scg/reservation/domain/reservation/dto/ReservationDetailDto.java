package edu.skku.scg.reservation.domain.reservation.dto;

import edu.skku.scg.reservation.domain.user.dto.UserSummaryDto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReservationDetailDto(
        Long id,
        UserSummaryDto user,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}