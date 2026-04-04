package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;


@Builder
public record ApprovalMethodResponse(
        String approvalMethod
) {}
