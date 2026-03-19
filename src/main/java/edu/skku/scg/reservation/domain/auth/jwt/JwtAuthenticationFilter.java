package edu.skku.scg.reservation.domain.auth.jwt;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.dto.AccessToken;
import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final HandlerExceptionResolver exceptionResolver;

    JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtProvider = jwtProvider;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromCookie(request);

        try {
            authenticateUser(request, token);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            exceptionResolver.resolveException(request, response, null, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, AuthConstants.ACCESS_TOKEN_COOKIE_NAME);
        return (cookie != null) ? cookie.getValue() : null;
    }

    private void authenticateUser(@NonNull HttpServletRequest request, String token) {
        if (StringUtils.hasText(token)) {
            AccessToken accessToken;
            try {
                accessToken = jwtProvider.parseAccessToken(token);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, e);
            }

            List<GrantedAuthority> authorities = Collections.emptyList();

            UserPrincipal principal = new UserPrincipal(accessToken.userId(), accessToken.type(), accessToken.managedUnitIds(), authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}