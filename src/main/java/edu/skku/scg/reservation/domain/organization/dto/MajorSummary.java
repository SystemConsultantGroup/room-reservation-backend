package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.organization.entity.Major;

public record MajorSummary(
        Long id,
        String name
) {
    public static MajorSummary from(Major major) {
        return new MajorSummary(major.getId(), major.getName());
    }
}
