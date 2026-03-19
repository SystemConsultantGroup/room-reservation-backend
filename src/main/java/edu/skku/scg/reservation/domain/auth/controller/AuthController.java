package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginRequestDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResponseDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.SignupRequestDto;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
            @Value("${jwt.accessToken.expiration}") long jwtExpiration,
            @Value("${cookie.secure}") boolean cookieSecure) {

        this.authService = authService;
        this.jwtExpiration = jwtExpiration;
        this.cookieSecure = cookieSecure;
    }

    @Operation(
            summary = "로그인",
            description = "구글 Credential을 통해 로그인을 진행하고 JWT 쿠키를 발급합니다. " +
            "신규 사용자인 경우 응답 본문을 통해 임시 회원 가임 토큰을 반환합니다.")
    @PublicApi
    @PostMapping("/google")
    public ResponseEntity<GoogleLoginResponseDto> googleLogin(
            @Valid @RequestBody GoogleLoginRequestDto dto,
            HttpServletResponse response) {

        GoogleLoginResult loginResult = authService.verifyGoogleTokenAndLogin(dto.credential());

        if (!loginResult.isNewUser()) {
            setAccessTokenCookie(response, loginResult.accessToken());
            return ResponseEntity.ok(
                    GoogleLoginResponseDto.builder()
                            .isNewUser(false)
                            .email(loginResult.email())
                            .name(loginResult.name())
                            .build()
            );
        } else {
            return ResponseEntity.ok(
                    GoogleLoginResponseDto.builder()
                            .isNewUser(true)
                            .registerToken(loginResult.registerToken())
                            .email(loginResult.email())
                            .name(loginResult.name())
                            .build()
            );
        }
    }

    @Operation(
            summary = "회원 가입",
            description = "회원 가입 토큰을 통해 신규 사용자를 등록합니다.")
    @PublicApi
    @PostMapping("/signup")
    public void signup(
            @Valid @RequestBody SignupRequestDto dto,
            HttpServletResponse response) {
        String accessToken = authService.registerNewUser(dto.registerToken(), dto.studentId(), dto.type());

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