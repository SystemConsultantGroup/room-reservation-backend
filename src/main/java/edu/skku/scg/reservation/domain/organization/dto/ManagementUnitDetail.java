package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;


public record ManagementUnitDetail(
        Long id,
        String name,
        String approvalMethod,
        String noticeTitle,
        String noticeContent
) {
    public static ManagementUnitDetail from(ManagementUnit managementUnit) {
        return new ManagementUnitDetail(
                managementUnit.getId(),
                managementUnit.getName(),
                managementUnit.getApprovalMethod(),
                managementUnit.getNoticeTitle(),
                managementUnit.getNoticeContent()
        );
    }
}
