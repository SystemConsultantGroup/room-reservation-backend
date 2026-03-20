package edu.skku.scg.reservation.domain.auth.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AccessToken(
        Long userId,
        UserType type,
        List<Long> managingUnitIds,
        LocalDateTime expiresAt
) {}
