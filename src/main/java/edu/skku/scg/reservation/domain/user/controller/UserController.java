package edu.skku.scg.reservation.domain.user.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.dto.GetMeResponse;
import edu.skku.scg.reservation.domain.user.dto.UpdateMeRequest;
import edu.skku.scg.reservation.domain.user.dto.UserDetail;
import edu.skku.scg.reservation.domain.user.dto.UserInfo;
import edu.skku.scg.reservation.domain.user.service.UserService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @Valid @RequestBody UpdateMeRequest dto) {
        userService.updateMe(userPrincipal.getId(), dto);
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

    @Operation(summary = "유저 상세 정보 조회")
    @AdminApi
    @GetMapping("/{userId}")
    public UserDetail getUserDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userId) {
        return userService.getUserDetail(userId, userPrincipal.getManagingUnitIds());
    }

    @Operation(summary = "유저의 향후 모든 예약 취소")
    @AdminApi
    @DeleteMapping("/{userId}/reservations")
    public void cancelAllFutureReservations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userId) {
        userService.cancelAllFutureReservations(userId, userPrincipal.getManagingUnitIds());
    }
}
