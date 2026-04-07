package edu.skku.scg.reservation.domain.auth.dto;

public record GoogleLoginResult(
        Boolean isGuest,
        String accessToken
) {}
