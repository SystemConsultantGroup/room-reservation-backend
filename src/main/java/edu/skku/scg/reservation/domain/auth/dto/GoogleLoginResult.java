package edu.skku.scg.reservation.domain.auth.dto;

import lombok.Builder;

@Builder
public record GoogleLoginResult(
        Boolean isGuest,
        String accessToken
) {}
