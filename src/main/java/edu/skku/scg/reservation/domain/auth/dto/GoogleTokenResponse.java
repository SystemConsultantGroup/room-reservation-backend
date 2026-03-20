package edu.skku.scg.reservation.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("id_token") String idToken,
        @JsonProperty("access_token") String accessToken
) {}