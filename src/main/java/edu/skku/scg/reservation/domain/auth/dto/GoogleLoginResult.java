package edu.skku.scg.reservation.domain.auth.dto;

import lombok.Builder;

@Builder
public record GoogleLoginResult(
        Boolean isNewUser,
        String accessToken,
        String registerToken,
        String email,
        String name
) {}
