package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;

import java.util.List;

public record UserDetail(
        Long id,
        String email,
        String name,
        String studentId,
        UserType type,
        List<MajorInfo> majors,
        List<Long> managingUnitIds
) {
    public static UserDetail from(User user, List<Long> managingUnitIds) {
        UserInfo userInfo = UserInfo.from(user);
        return new UserDetail(
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
