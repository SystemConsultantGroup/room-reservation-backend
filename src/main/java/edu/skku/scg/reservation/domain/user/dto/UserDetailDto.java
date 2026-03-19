package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
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

        @Schema(description = "유저 타입", example = "STUDENT")
        UserType type,

        @Schema(description = "관리 권한을 가진 운영 단위의 ID 목록", example = "[3]")
        List<Long> managedUnitIds
) {}