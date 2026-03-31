package edu.skku.scg.reservation.domain.user.dto;

import lombok.Builder;

@Builder
public record UserSummary(
        Long id,
        String name
) {}