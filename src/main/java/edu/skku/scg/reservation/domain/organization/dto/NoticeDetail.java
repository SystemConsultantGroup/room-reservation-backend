package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;

@Builder
public record NoticeDetail(
        String title,
        String content
) {}