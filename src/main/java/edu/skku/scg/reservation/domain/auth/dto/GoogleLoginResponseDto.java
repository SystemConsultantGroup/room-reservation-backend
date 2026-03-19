package edu.skku.scg.reservation.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record GoogleLoginResponseDto(
        @Schema(description = "신규 가입 대상자 여부", example = "true")
        Boolean isNewUser,

        @Schema(description = "신규 유저(isNewUser=true)에게만 발급되는 임시 회원 가입용 토큰", example = "eyJhbGciOiJSUzI1NiIs...")
        String registerToken,

        @Schema(description = "이메일", example = "example@g.skku.edu")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name
) {
}