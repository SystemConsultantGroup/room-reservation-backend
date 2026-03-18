package edu.skku.scg.reservation.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "사용자 상세 정보 응답 DTO")
public record UserDetailDto(
        @Schema(description = "유저 고유 식별자", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "example@g.skku.edu")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "학번", example = "2026000000")
        String studentId,

        @Schema(description = "유저가 승인받은 단과대 ID 리스트", example = "[1, 3]")
        List<Long> approvedCids,

        @Schema(description = "유저가 어드민 권한을 가진 단과대 ID 리스트", example = "[1]")
        List<Long> adminCids
) {}