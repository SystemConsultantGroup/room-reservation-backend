package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorInfo;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;

import java.util.List;

public record UserInfo(
        Long id,
        String email,
        String name,
        String studentId,
        UserType type,
        List<MajorInfo> majors
) {
    public static UserInfo from(User user) {
        List<MajorInfo> majors = user.getUserMajors().stream()
                .filter(userMajor -> userMajor.getStatus() == RegistrationStatus.APPROVED)
                .map(MajorInfo::from)
                .toList();

        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getType(),
                majors
        );
    }
}
