package edu.skku.scg.reservation.domain.user.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.dto.UserDetailDto;
import edu.skku.scg.reservation.domain.user.service.UserService;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "유저 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public UserDetailDto getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userService.getUserDetail(userPrincipal);
    }

    @Operation(summary = "유저 목록 조회")
    @AdminApi
    @GetMapping
    public List<UserDetailDto> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return null;
    }
}