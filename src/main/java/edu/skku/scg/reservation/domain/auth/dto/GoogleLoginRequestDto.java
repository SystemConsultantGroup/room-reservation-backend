package edu.skku.scg.reservation.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GoogleLoginRequestDto(
        @Schema(description = "구글 로그인 성공 후 발급받은 ID Token", example = "eyJhbGciOiJSUzI1NiIs...")
        @NotBlank(message = "구글 인증 정보는 필수입니다.")
        String credential,

        @Schema(description = "사용자의 학번", example = "2026000000")
        @Pattern(regexp = "^$|[0-9]{10}", message = "학번은 비어있거나 10자리의 숫자여야 합니다.")
        String studentId
) {
}