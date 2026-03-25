package edu.skku.scg.reservation.domain.room.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.room.dto.*;
import edu.skku.scg.reservation.domain.room.service.RoomService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import edu.skku.scg.reservation.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "공간 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "공간 생성")
    @AdminApi
    @PostMapping
    public RoomDetailDto createRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RoomCreateRequestDto dto) {

        return roomService.createRoom(dto, userPrincipal.getManagingUnitIds());
    }

    @Operation(summary = "공간 상세 조회")
    @PublicApi
    @GetMapping("/{roomId}")
    public RoomDetailDto getRoom(
            @PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    @Operation(summary = "공간 정보 수정")
    @AdminApi
    @PutMapping("/{roomId}")
    public RoomDetailDto updateRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateRequestDto dto) {
        return roomService.updateRoom(
                roomId,
                dto,
                userPrincipal.getManagingUnitIds()
        );
    }

    @Operation(summary = "공간 삭제")
    @AdminApi
    @DeleteMapping("/{roomId}")
    public void deleteRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long roomId) {
        roomService.deleteRoom(roomId, userPrincipal.getManagingUnitIds());
    }

    @Operation(summary = "특정 날짜의 공간 스케줄 목록 조회")
    @PublicApi
    @GetMapping("/schedules")
    public PageResponse<DailyRoomScheduleResponseDto> getDailyRoomSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long managementUnitId = 1L; // TODO: ORIGIN 헤더로부터 추출

        Pageable pageable = PageRequest.of(page, size);

        Page<DailyRoomScheduleResponseDto> rooms = roomService.getDailyRoomSchedules(managementUnitId, date, pageable);

        return PageResponse.of(rooms);
    }

    @Operation(summary = "특정 주차의 공간 스케줄 목록 조회")
    @PublicApi
    @GetMapping("{roomId}/schedules")
    public WeeklyRoomScheduleResponseDto getWeeklyRoomSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable Long roomId
            ) {

        return null;
    }
}