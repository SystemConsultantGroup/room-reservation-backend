package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;

@Builder
public record MajorSummaryDto (
        Long id,
        String name
) {}