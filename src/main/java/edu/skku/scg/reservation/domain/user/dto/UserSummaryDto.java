package edu.skku.scg.reservation.domain.user.dto;

import lombok.Builder;

@Builder
public record UserSummaryDto(
        Long id,
        String name
) {}