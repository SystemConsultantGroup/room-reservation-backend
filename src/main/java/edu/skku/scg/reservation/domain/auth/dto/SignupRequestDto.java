package edu.skku.scg.reservation.domain.auth.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupRequestDto(
        @Schema(description = "임시 회원 가입용 토큰", example = "eyJhbGciOiJSUzI1NiIs...")
        @NotBlank(message = "Register Token은 필수입니다.")
        String registerToken,

        @Schema(description = "학번", example = "2022000000")
        String studentId,

        @Schema(description = "유저 타입", example = "STUDENT")
        @NotNull(message = "유저 타입은 필수입니다.")
        UserType type
) {
}