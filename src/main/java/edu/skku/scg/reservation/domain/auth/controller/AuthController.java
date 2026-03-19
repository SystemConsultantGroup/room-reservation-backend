package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginRequestDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResponseDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.OnboardingRequestDto;
import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "인증 API", description = "구글 로그인 및 로그아웃을 담당하는 API입니다.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final long jwtExpiration;
    private final boolean cookieSecure;

    AuthController(
            AuthService authService,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${cookie.secure}") boolean cookieSecure) {

        this.authService = authService;
        this.jwtExpiration = jwtExpiration;
        this.cookieSecure = cookieSecure;
    }

    @Operation(
            summary = "구글 로그인 및 회원가입",
            description = "구글 ID 토큰을 통해 로그인을 진행하고 JWT 쿠키를 발급합니다. " +
                    "신규 유저일 경우 회원가입을 진행합니다.")
    @PublicApi
    @PostMapping("/google")
    public GoogleLoginResponseDto googleLogin(
            @Valid @RequestBody GoogleLoginRequestDto dto,
            HttpServletResponse response) {

        GoogleLoginResult loginResult = authService.verifyGoogleTokenAndLogin(dto.credential());

        setAccessTokenCookie(response, loginResult.accessToken());

        return GoogleLoginResponseDto.builder()
                .isNewUser(loginResult.isNewUser())
                .email(loginResult.email())
                .name(loginResult.name())
                .build();
    }

    @Operation(
            summary = "추가 정보 등록",
            description = "GUEST 유저의 학번과 타입을 등록하여 정식 권한을 획득하고 JWT 쿠키를 발급합니다.")
    @PostMapping("/onboarding")
    public void onboarding(
            @Valid @RequestBody OnboardingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletResponse response) {
        String accessToken = authService.completeOnboarding(userPrincipal.getId(), dto.type(), dto.studentId());

        setAccessTokenCookie(response, accessToken);
    }

    @Operation(summary = "로그아웃", description = "액세스 토큰 쿠키를 만료시킵니다.")
    @PublicApi
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        clearAccessTokenCookie(response);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(AuthConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpiration))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from(AuthConstants.ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }
}