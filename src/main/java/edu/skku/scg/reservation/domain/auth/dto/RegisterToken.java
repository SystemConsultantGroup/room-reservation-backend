package edu.skku.scg.reservation.domain.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegisterToken(
        String googleId,
        String email,
        String name,
        LocalDateTime expiresAt
) {}
