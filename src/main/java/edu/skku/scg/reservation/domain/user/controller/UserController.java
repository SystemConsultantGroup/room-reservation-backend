package edu.skku.scg.reservation.domain.user.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.organization.dto.MajorApplicationList;
import edu.skku.scg.reservation.domain.user.dto.*;
import edu.skku.scg.reservation.domain.user.service.UserService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public GetMeResponse getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userService.getUserDetail(userPrincipal);
    }

    @Operation(summary = "내 정보 변경")
    @PatchMapping("/me")
    public void updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateMeRequest dto) {
        // 해당 유저의 정보를 dto에 따라 변경한다.
    }

    @Operation(summary = "유저 목록 조회")
    @AdminApi
    @GetMapping
    public PageResponse<UserInfo> getUsers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return PageResponse.of(userService.getUsers(userPrincipal.getManagingUnitIds(), pageable, keyword));
    }

    @Operation(summary = "유저 유형 변경")
    @AdminApi
    @PatchMapping("/{userId}/type")
    public void updateUserType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateUserTypeRequest dto) {
        // 이 API를 호출하려면 해당 user와 연결된 major의 managementUnitId 중 하나 이상이 userPrincipal.getManagingUnitIds()에 속해야함
    }

    @Operation(summary = "유저 상세 정보 조회")
    @AdminApi
    @GetMapping("/{userId}")
    public UserDetail getMyApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 이 API를 호출하려면 해당 user와 연결된 major의 managementUnitId 중 하나 이상이 userPrincipal.getManagingUnitIds()에 속해야함
    }

    @Operation(summary = "유저의 향후 모든 예약 취소")
    @AdminApi
    @DeleteMapping("/{userId}/reservations")
    public void cancelAllFutureReservations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userId) {
        // 이 API를 호출하려면 해당 user와 연결된 major의 managementUnitId 중 하나 이상이 userPrincipal.getManagingUnitIds()에 속해야함
        // ReservationService에 userId와 userPrincipal.getManagingUnitIds()를 받는 새 메서드를 만들어서
        // 해당 user의 향후 모든 예약을 bulk delete한다.
        // 다만, 예약의 room과 연결된 전공들의 managementUnitId 목록이 모두 userPrincipal.getManagingUnitIds()에 속한 예약만 삭제한다.
        // (roomRepository.findRoomsByManagementUnitIds 참고)
    }
}