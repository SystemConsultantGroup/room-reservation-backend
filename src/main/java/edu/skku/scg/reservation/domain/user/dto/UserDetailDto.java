package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "사용자 상세 정보 응답 DTO")
public record UserDetailDto(
        Long id,

        String email,

        String name,

        String studentId,

        UserType type,

        List<Long> managingUnitIds
) {}