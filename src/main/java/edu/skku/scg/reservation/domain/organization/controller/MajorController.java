package edu.skku.scg.reservation.domain.organization.controller;

import edu.skku.scg.reservation.domain.organization.dto.MajorApplicationDetailDto;
import edu.skku.scg.reservation.domain.organization.dto.MajorApplicationRequestDto;
import edu.skku.scg.reservation.domain.organization.dto.MajorSummaryDto;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "전공 API")
@RestController
@RequestMapping("/majors")
public class MajorController {

    @Operation(summary = "전공 추가 등록 신청")
    @PostMapping("/apply")
    public void applyMajor(@RequestBody MajorApplicationRequestDto dto) {
    }

    @Operation(summary = "전공 등록 신청 목록 조회")
    @AdminApi
    @GetMapping("/applications")
    public PageResponse<MajorApplicationDetailDto> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return null;
    }

    @Operation(summary = "전공 등록 신청 승인")
    @AdminApi
    @PostMapping("/applications/{applicatoinId}/approve")
    public void approveApplication(
            @PathVariable Long applicatoinId
    ) {
    }

    @Operation(summary = "전공 등록 신청 거절")
    @AdminApi
    @PostMapping("/applications/{applicatoinId}/reject")
    public void rejectApplication(
            @PathVariable Long applicatoinId
    ) {
    }

    @Operation(summary = "전공 목록 조회")
    @GetMapping
    public List<MajorSummaryDto> getMajors() {
        return null;
    }
}
