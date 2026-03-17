package edu.skku.scg.reservation.domain.auth.dto;

import java.util.List;

public record LoginResult(
        String accessToken,
        Long userId,
        List<Long> approvedCids,
        List<Long> adminCids
) {}
