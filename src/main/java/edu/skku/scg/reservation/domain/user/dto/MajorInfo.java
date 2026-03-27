package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.MajorType;
import lombok.Builder;

@Builder
public record MajorInfo (
    Long id,
    String name,
    MajorType type
) {
}
