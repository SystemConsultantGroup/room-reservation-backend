package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.AuthResponseDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginRequestDto;
import edu.skku.scg.reservation.domain.auth.dto.LoginResult;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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

    @PostMapping("/google")
    public AuthResponseDto googleLogin(
            @Valid @RequestBody GoogleLoginRequestDto request,
            HttpServletResponse response) {

        LoginResult loginResult = authService.verifyGoogleTokenAndLogin(request.credential(), request.studentId());

        setAccessTokenCookie(response, loginResult.accessToken());

        return new AuthResponseDto(loginResult.userId(), loginResult.approvedCids(), loginResult.adminCids());
    }

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