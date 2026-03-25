package edu.skku.scg.reservation.domain.organization.dto;

import java.util.List;

public record MajorApplicationRequestDto(
    List<MajorRequest> majors
) {
}
