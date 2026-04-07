package edu.skku.scg.reservation.domain.auth.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;

import java.time.LocalDateTime;
import java.util.List;

public record AccessToken(
        Long userId,
        UserType type,
        List<Long> managingUnitIds,
        LocalDateTime expiresAt
) {}
