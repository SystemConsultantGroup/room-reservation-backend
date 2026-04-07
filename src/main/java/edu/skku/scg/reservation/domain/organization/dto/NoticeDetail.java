package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.organization.entity.ManagementUnit;

public record NoticeDetail(
        String title,
        String content
) {
    public static NoticeDetail from(ManagementUnit managementUnit) {
        return new NoticeDetail(managementUnit.getNoticeTitle(), managementUnit.getNoticeContent());
    }
}
