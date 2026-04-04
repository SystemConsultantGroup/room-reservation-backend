package edu.skku.scg.reservation.domain.organization.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.organization.dto.*;
import edu.skku.scg.reservation.domain.organization.service.MajorService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import edu.skku.scg.reservation.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "전공 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/majors")
public class MajorController {

    private final MajorService majorService;

    @Operation(summary = "전공 등록 방법 조회")
    @PublicApi
    @GetMapping("/approvalMethod")
    public ApprovalMethodResponse getApprovalMethod(@ManagementUnitId Long managementUnitId) {
        return ApprovalMethodResponse.builder()
                .approvalMethod(majorService.getApprovalMethod(managementUnitId))
                .build();
    }

    @Operation(summary = "전공 추가 등록 신청")
    @PostMapping("/apply")
    public void applyMajor(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MajorApplicationRequest dto) {

        majorService.applyMajor(userPrincipal.getId(), dto.majors());
    }

    @Operation(summary = "전공 등록 신청 목록 조회")
    @AdminApi
    @GetMapping("/applications")
    public PageResponse<MajorApplicationDetail> getApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        return PageResponse.of(majorService.getApplications(userPrincipal.getManagingUnitIds(), pageable, keyword));
    }

    @Operation(summary = "나의 전공 등록 신청 현황 조회")
    @GetMapping("/applications/me")
    public MajorApplicationList getMyApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return majorService.getApplications(userPrincipal.getId());
    }

    @Operation(summary = "전공 등록 신청 승인")
    @AdminApi
    @PostMapping("/applications/{applicationId}/approve")
    public void approveApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        majorService.approveApplication(applicationId, userPrincipal.getManagingUnitIds());
    }

    @Operation(summary = "전공 등록 신청 거절")
    @AdminApi
    @PostMapping("/applications/{applicationId}/reject")
    public void rejectApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        majorService.rejectApplication(applicationId, userPrincipal.getManagingUnitIds());
    }

    @Operation(summary = "전공 목록 조회")
    @PublicApi
    @GetMapping
    public List<MajorSummary> getMajors(
            @ManagementUnitId Long managementUnitId) {

        return majorService.getMajorSummaries(managementUnitId);
    }

    @Operation(summary = "관리 권한이 있는 전공 목록 조회")
    @AdminApi
    @GetMapping("/managed")
    public List<MajorSummary> getManagedMajors(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return majorService.getMajorSummaries(userPrincipal.getManagingUnitIds());
    }
}
