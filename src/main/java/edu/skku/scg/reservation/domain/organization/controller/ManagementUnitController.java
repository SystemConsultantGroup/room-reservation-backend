package edu.skku.scg.reservation.domain.organization.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.organization.dto.*;
import edu.skku.scg.reservation.domain.organization.service.ManagementUnitService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리 단위 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/managementUnit")
public class ManagementUnitController {

    private final ManagementUnitService managementUnitService;

    @Operation(summary = "관리 단위 정보 조회")
    @PublicApi
    @GetMapping
    public ManagementUnitDetail getManagementUnit(@ManagementUnitId Long managementUnitId) {
        return managementUnitService.getManagementUnit(managementUnitId);
    }

    @Operation(summary = "공지 수정")
    @AdminApi
    @PutMapping("/notice")
    public void updateNotice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ManagementUnitId Long managementUnitId,
            @Valid @RequestBody UpdateNoticeRequest dto) {

        managementUnitService.updateNotice(managementUnitId, dto, userPrincipal.getManagingUnitIds());
    }
}
