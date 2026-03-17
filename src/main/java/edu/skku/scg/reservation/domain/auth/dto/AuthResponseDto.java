package edu.skku.scg.reservation.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AuthResponseDto(
        @Schema(description = "유저 고유 식별자", example = "1")
        Long userId,

        @Schema(description = "유저가 승인받은 단과대 ID 리스트", example = "[1, 3]")
        List<Long> approvedCids,

        @Schema(description = "유저가 어드민 권한을 가진 단과대 ID 리스트", example = "[1]")
        List<Long> adminCids
) {}
