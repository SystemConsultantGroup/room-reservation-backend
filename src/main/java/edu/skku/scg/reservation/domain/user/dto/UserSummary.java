package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.User;

public record UserSummary(
        Long id,
        String name
) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getName());
    }
}
