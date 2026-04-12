package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorApplication;
import edu.skku.scg.reservation.domain.organization.dto.MajorInfo;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;

import java.util.List;

public record UserDetail(
        Long id,
        String email,
        String name,
        String studentId,
        UserType type,
        List<MajorApplication> applications
) {
    public static UserDetail from(User user) {
        List<MajorApplication> applications = user.getUserMajors().stream()
                .map(MajorApplication::from)
                .toList();

        return new UserDetail(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getType(),
                applications
        );
    }
}
