package edu.skku.scg.reservation.domain.organization.controller;

import edu.skku.scg.reservation.domain.organization.dto.NoticeDetailDto;
import edu.skku.scg.reservation.domain.organization.dto.UpdateNoticeRequestDto;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공지 API")
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Operation(summary = "공지 조회")
    @PublicApi
    @GetMapping
    public NoticeDetailDto getNotice() {
        return null;
    }

    @Operation(summary = "공지 수정")
    @AdminApi
    @PutMapping
    public void updateNotice(UpdateNoticeRequestDto dto) {
    }
}
