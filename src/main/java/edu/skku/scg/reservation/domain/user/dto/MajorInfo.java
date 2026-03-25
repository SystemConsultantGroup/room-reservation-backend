package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.MajorType;

public record MajorInfo (
    Long id,
    String name,
    MajorType type
) {
}
