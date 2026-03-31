package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.dto.UserInfo;
import edu.skku.scg.reservation.domain.user.entity.MajorType;
import lombok.Builder;

import java.util.List;

@Builder
public record MajorApplicationDetail(
        UserInfo user,
        List<MajorApplication> applications
) {
    @Builder
    public record MajorApplication(
            Long id,
            MajorSummary major,
            MajorType type
    ) {}
}
