package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.dto.UserDetailDto;
import edu.skku.scg.reservation.domain.user.entity.MajorType;

import java.util.List;

public record MajorApplicationDetailDto (
    UserDetailDto user,
    List<MajorApplication> applications
) {
    public record MajorApplication(
        MajorSummaryDto major,
        MajorType type
    ) {
    }
}
