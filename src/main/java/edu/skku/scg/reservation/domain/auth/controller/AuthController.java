package edu.skku.scg.reservation.domain.auth.controller;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.OnboardingRequestDto;
import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.auth.service.AuthService;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Tag(name = "인증 API", description = "구글 로그인 및 로그아웃을 담당하는 API입니다.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final long jwtExpiration;
    private final boolean cookieSecure;
    private final List<String> allowedDomains;

    AuthController(
            AuthService authService,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${cookie.secure}") boolean cookieSecure,
            @Value("${oauth.redirect-uri-whitelist}") List<String> allowedDomains) {

        this.authService = authService;
        this.jwtExpiration = jwtExpiration;
        this.cookieSecure = cookieSecure;
        this.allowedDomains = allowedDomains;
    }

    @Operation(
            summary = "구글 로그인 시작",
            description = "구글 로그인 페이지로 리다이렉트합니다.",
            responses = {
                @ApiResponse(responseCode = "302")
            }
    )
    @PublicApi
    @GetMapping("/login/google")
    public void redirectToGoogle(
            @RequestParam(value = "redirectUri") String redirectUri,
            HttpServletResponse response) throws IOException {

        String googleAuthUrl = authService.getGoogleAuthUrl(redirectUri);
        response.sendRedirect(googleAuthUrl);
    }

    @Operation(
            summary = "구글 OAuth2 콜백",
            description = "구글 인증 후 코드를 받아 처리를 완료하고 리다이렉트합니다. JWT 쿠키를 발급합니다.",
            responses = {
                @ApiResponse(responseCode = "302")
            }
    )
    @PublicApi
    @GetMapping("/callback/google")
    public void googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state") String state,
            HttpServletResponse response) throws IOException {

        validateRedirectUri(state);

        GoogleLoginResult loginResult = authService.processGoogleCallback(code);

        setAccessTokenCookie(response, loginResult.accessToken());

        String targetUrl = UriComponentsBuilder.fromUriString(state)
                .queryParam("isGuest", loginResult.isGuest())
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }

    @Operation(
            summary = "추가 정보 등록",
            description = "GUEST 유저의 학번과 타입, 소속 전공을 등록하여 정식 권한을 획득하고 JWT 쿠키를 발급합니다. " +
                    "추가로 소속 전공을 통해 승인 요청을 생성합니다.")
    @PostMapping("/onboarding")
    public void onboarding(
            @Valid @RequestBody OnboardingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletResponse response) {
        String accessToken = authService.completeOnboarding(userPrincipal.getId(), dto);

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

    public void validateRedirectUri(String redirectUri) {
        if (redirectUri.startsWith("/") && !redirectUri.startsWith("//")) {
            return;
        }

        try {
            java.net.URI uri = java.net.URI.create(redirectUri);
            String host = uri.getHost();

            if (host == null) {
                throw new BusinessException(ErrorCode.INVALID_REDIRECT_URL);
            }

            boolean isAllowed = allowedDomains.stream()
                    .anyMatch(allowed -> isStrictMatch(host, allowed));

            if (isAllowed) {
                return;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REDIRECT_URL);
        }

        throw new BusinessException(ErrorCode.INVALID_REDIRECT_URL);
    }

    private boolean isStrictMatch(String host, String allowed) {
        if (host.equalsIgnoreCase(allowed)) {
            return true;
        }
        return host.toLowerCase().endsWith("." + allowed.toLowerCase());
    }
}