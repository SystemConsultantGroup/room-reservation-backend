package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;

@Builder
public record UpdateNoticeRequest(
        String title,
        String content
) {}