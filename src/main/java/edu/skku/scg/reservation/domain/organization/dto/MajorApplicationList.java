package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;

import java.util.List;

public record MajorApplicationList(
        List<MajorApplication> applications
) {
    public static MajorApplicationList from(User user, boolean includeApproved) {
        List<MajorApplication> applications = user.getUserMajors().stream()
                .filter(um -> um.getStatus() != RegistrationStatus.APPROVED || includeApproved)
                .map(MajorApplication::from)
                .toList();
        return new MajorApplicationList(applications);
    }
}
