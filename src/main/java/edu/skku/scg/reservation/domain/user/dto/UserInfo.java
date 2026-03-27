package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;
import lombok.Builder;

import java.util.List;

@Builder
public record UserInfo(
        Long id,
        String email,
        String name,
        String studentId,
        UserType type,
        List<MajorInfo> majors
) {}