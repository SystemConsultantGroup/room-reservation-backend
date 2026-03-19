package edu.skku.scg.reservation.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GoogleLoginRequestDto(
        @Schema(description = "구글 로그인 성공 후 발급받은 ID Token", example = "eyJhbGciOiJSUzI1NiIs...")
        @NotBlank(message = "구글 인증 정보는 필수입니다.")
        String credential
) {
}