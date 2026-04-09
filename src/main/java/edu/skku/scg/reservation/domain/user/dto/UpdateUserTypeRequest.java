package edu.skku.scg.reservation.domain.user.dto;

import edu.skku.scg.reservation.domain.user.entity.UserType;

public record UpdateUserTypeRequest(
        UserType type
) {}
