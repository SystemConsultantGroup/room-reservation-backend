package edu.skku.scg.reservation.domain.user.controller;

import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.dto.UserDetailDto;
import edu.skku.scg.reservation.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 API", description = "사용자 정보 조회 및 관리를 담당하는 API입니다.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 상세 정보를 반환합니다.")
    @GetMapping("/me")
    public UserDetailDto getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userService.getUserDetail(userPrincipal);
    }
}