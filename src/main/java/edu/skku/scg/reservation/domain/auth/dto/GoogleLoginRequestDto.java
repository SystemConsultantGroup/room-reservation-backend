package edu.skku.scg.reservation.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GoogleLoginRequestDto(
        @NotBlank(message = "구글 인증 정보는 필수입니다.")
        String credential,

        @Pattern(regexp = "^$|[0-9]{10}", message = "학번은 비어있거나 10자리의 숫자여야 합니다.")
        String studentId
) {
}