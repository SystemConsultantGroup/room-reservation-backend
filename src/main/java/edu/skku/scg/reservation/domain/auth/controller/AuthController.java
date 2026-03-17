package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.AuthResponseDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginRequestDto;
import edu.skku.scg.reservation.domain.auth.dto.LoginResult;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
            summary = "로그인",
            description = "구글 Credential과 학번을 이용해 로그인을 진행하고 JWT 쿠키를 발급합니다. " +
            "최초 로그인시 회원가입을 수행합니다.")
    @PublicApi
    @PostMapping("/google")
    public AuthResponseDto googleLogin(
            @Valid @RequestBody GoogleLoginRequestDto request,
            HttpServletResponse response) {

        LoginResult loginResult = authService.verifyGoogleTokenAndLogin(request.credential(), request.studentId());

        setAccessTokenCookie(response, loginResult.accessToken());

        return new AuthResponseDto(loginResult.userId(), loginResult.approvedCids(), loginResult.adminCids());
    }

    @Operation(summary = "로그아웃", description = "액세스 토큰 쿠키를 만료시킵니다.")
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