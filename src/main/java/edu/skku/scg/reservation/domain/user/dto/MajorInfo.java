package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.MajorType;
import edu.skku.scg.reservation.domain.user.entity.UserMajor;

public record MajorInfo (
    Long id,
    String name,
    MajorType type
) {
    public static MajorInfo from(UserMajor userMajor) {
        return new MajorInfo(
                userMajor.getMajor().getId(),
                userMajor.getMajor().getName(),
                userMajor.getType()
        );
    }
}
