package edu.skku.scg.reservation.domain.organization.dto;

import edu.skku.scg.reservation.domain.user.dto.UserInfo;
import edu.skku.scg.reservation.domain.user.entity.RegistrationStatus;
import edu.skku.scg.reservation.domain.user.entity.User;

import java.util.List;

public record MajorApplicationDetail(
        UserInfo user,
        List<MajorApplication> applications
) {
    public static MajorApplicationDetail from(User user, List<Long> managingUnitIds) {
        List<MajorApplication> pendingApplications = user.getUserMajors().stream()
                .filter(um -> um.getStatus() == RegistrationStatus.PENDING)
                .filter(um -> managingUnitIds.contains(um.getMajor().getManagementUnit().getId()))
                .map(MajorApplication::from)
                .toList();
        return new MajorApplicationDetail(UserInfo.from(user), pendingApplications);
    }
}
