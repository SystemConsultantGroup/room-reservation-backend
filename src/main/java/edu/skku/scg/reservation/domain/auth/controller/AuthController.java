package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.OnboardingRequestDto;
import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import edu.skku.scg.reservation.domain.organization.service.OriginService;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import edu.skku.scg.reservation.global.util.HttpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@Tag(name = "인증 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OriginService originService;
    private final long jwtExpiration;
    private final boolean cookieSecure;

    AuthController(
            AuthService authService,
            OriginService originService,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${cookie.secure}") boolean cookieSecure) {

        this.authService = authService;
        this.originService = originService;
        this.jwtExpiration = jwtExpiration;
        this.cookieSecure = cookieSecure;
    }

    @Operation(
            summary = "구글 로그인 시작",
            responses = {
                @ApiResponse(responseCode = "302")
            }
    )
    @PublicApi
    @GetMapping("/login/google")
    public ResponseEntity<Void> redirectToGoogle(HttpServletRequest request) {
        String redirectUri = HttpUtils.reconstructOrigin(request);
        String googleAuthUrl = authService.getGoogleAuthUrl(redirectUri);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleAuthUrl))
                .build();
    }

    @Operation(
            summary = "구글 OAuth2 콜백",
            responses = {
                @ApiResponse(responseCode = "302")
            }
    )
    @PublicApi
    @GetMapping("/callback/google")
    public ResponseEntity<Void> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state") String originUrl,
            HttpServletResponse response) {

        originService.validateOriginUrl(originUrl);

        GoogleLoginResult loginResult = authService.processGoogleCallback(code);
        setAccessTokenCookie(response, loginResult.accessToken());

        UriComponentsBuilder targetUrlBuilder = UriComponentsBuilder.fromUriString(originUrl);

        if (loginResult.isGuest()) {
            targetUrlBuilder.queryParam("isGuest", "true");
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(targetUrlBuilder.toUriString()))
                .build();
    }

    @Operation(summary = "GUEST 유저 추가 정보 등록")
    @PatchMapping("/onboarding")
    public void onboarding(
            @Valid @RequestBody OnboardingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletResponse response) {
        String accessToken = authService.completeOnboarding(userPrincipal.getId(), dto);

        setAccessTokenCookie(response, accessToken);
    }

    @Operation(summary = "로그아웃")
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