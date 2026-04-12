package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;
import edu.skku.scg.reservation.domain.user.entity.UserType;


public record ManagementUnitDetail(
        Long id,
        String name,
        String approvalMethod,
        String noticeTitle,
        String noticeContent,
        UserType defaultUserType
) {
    public static ManagementUnitDetail from(ManagementUnit managementUnit) {
        return new ManagementUnitDetail(
                managementUnit.getId(),
                managementUnit.getName(),
                managementUnit.getApprovalMethod(),
                managementUnit.getNoticeTitle(),
                managementUnit.getNoticeContent(),
                managementUnit.getDefaultUserType()
        );
    }
}
