package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.organization.dto.MajorInfo;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;

import java.util.List;

public record GetMeResponse(
        Long id,
        String email,
        String name,
        String studentId,
        UserType type,
        List<MajorInfo> majors,
        List<Long> managingUnitIds
) {
    public static GetMeResponse from(User user, List<Long> managingUnitIds) {
        UserInfo userInfo = UserInfo.from(user);
        return new GetMeResponse(
                userInfo.id(),
                userInfo.email(),
                userInfo.name(),
                userInfo.studentId(),
                userInfo.type(),
                userInfo.majors(),
                managingUnitIds
        );
    }
}
