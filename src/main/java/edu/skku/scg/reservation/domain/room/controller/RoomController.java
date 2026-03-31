package edu.skku.scg.reservation.domain.room.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.room.dto.*;
import edu.skku.scg.reservation.domain.room.service.RoomService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import edu.skku.scg.reservation.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public void createRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RoomCreateRequest dto) {

        roomService.createRoom(dto, userPrincipal.getManagingUnitIds());
    }

    @Operation(summary = "공간 상세 조회")
    @PublicApi
    @GetMapping("/{roomId}")
    public RoomResponse getRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return roomService.getRoom(roomId, userPrincipal == null ? null : userPrincipal.getId());
    }

    @Operation(summary = "공간 정보 수정")
    @AdminApi
    @PutMapping("/{roomId}")
    public void updateRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateRequest dto) {
        roomService.updateRoom(
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
    public PageResponse<DailyRoomScheduleResponse> getDailyRoomSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ManagementUnitId Long managementUnitId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        Page<DailyRoomScheduleResponse> rooms = roomService.getDailyRoomSchedules(managementUnitId, date, pageable);

        return PageResponse.of(rooms);
    }

    @Operation(summary = "특정 주차의 공간 스케줄 목록 조회")
    @PublicApi
    @GetMapping("{roomId}/schedules")
    public WeeklyRoomScheduleResponse getWeeklyRoomSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable Long roomId) {
        return roomService.getWeeklyRoomSchedules(date, roomId);
    }

    @Operation(summary = "공간 목록 조회")
    @AdminApi
    @GetMapping
    public PageResponse<RoomInfo> getRooms(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        Page<RoomInfo> rooms = roomService.getRooms(userPrincipal.getManagingUnitIds(), pageable);

        return PageResponse.of(rooms);
    }

    @Operation(summary = "공간 목록 요약 조회")
    @PublicApi
    @GetMapping("/summary")
    public RoomSummaryList getRoomSummaries(
            @ManagementUnitId Long managementUnitId) {

        return roomService.getRoomSummaries(managementUnitId);
    }
}