package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;


@Builder
public record ManagementUnitDetail(
        Long id,
        String name,
        String approvalMethod,
        String noticeTitle,
        String noticeContent
) {}
