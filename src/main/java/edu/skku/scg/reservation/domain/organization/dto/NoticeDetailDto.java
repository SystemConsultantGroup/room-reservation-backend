package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;

@Builder
public record NoticeDetailDto (
        String title,
        String content
) {
}