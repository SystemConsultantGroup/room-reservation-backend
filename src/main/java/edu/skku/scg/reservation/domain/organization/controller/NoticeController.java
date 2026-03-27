package edu.skku.scg.reservation.domain.organization.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.organization.dto.NoticeDetailDto;
import edu.skku.scg.reservation.domain.organization.dto.UpdateNoticeRequestDto;
import edu.skku.scg.reservation.domain.organization.service.NoticeService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 조회")
    @PublicApi
    @GetMapping
    public NoticeDetailDto getNotice(@ManagementUnitId Long managementUnitId) {
        return noticeService.getNotice(managementUnitId);
    }

    @Operation(summary = "공지 수정")
    @AdminApi
    @PutMapping
    public void updateNotice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ManagementUnitId Long managementUnitId,
            @RequestBody UpdateNoticeRequestDto dto) {

        noticeService.updateNotice(managementUnitId, dto, userPrincipal.getManagingUnitIds());
    }
}
