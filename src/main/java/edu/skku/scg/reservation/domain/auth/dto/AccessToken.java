package edu.skku.scg.reservation.domain.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AccessToken(
        Long userId,
        List<Long> managedUnitIds,
        LocalDateTime expiresAt
) {}
