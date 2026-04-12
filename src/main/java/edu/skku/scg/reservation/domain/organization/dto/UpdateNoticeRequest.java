package edu.skku.scg.reservation.domain.organization.dto;

import jakarta.validation.constraints.Size;

public record UpdateNoticeRequest(
        @Size(max = 100)
        String title,
        @Size(max = 5000)
        String content
) {}
