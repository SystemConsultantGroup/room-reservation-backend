package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.dto.AuthResponseDto;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginRequestDto;
import edu.skku.scg.reservation.domain.auth.dto.LoginResult;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final long jwtExpiration;

    AuthController(AuthService authService, @Value("${jwt.expiration}") long jwtExpiration) {
        this.authService = authService;
        this.jwtExpiration = jwtExpiration;
    }

    @PostMapping("/google")
    public AuthResponseDto googleLogin(
            @RequestBody GoogleLoginRequestDto request,
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
        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtExpiration)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }
}