package edu.skku.scg.reservation.domain.organization.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MajorApplicationList(
        List<MajorApplication> applications
) {}
