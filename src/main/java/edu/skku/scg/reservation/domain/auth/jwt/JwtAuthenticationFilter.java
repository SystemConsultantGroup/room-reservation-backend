package edu.skku.scg.reservation.domain.auth.jwt;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.domain.auth.principal.UserPrincipal;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserRole;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver exceptionResolver;

    JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserRepository userRepository,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
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
            authenticateUser(request, response, token);
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

    private void authenticateUser(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, String token) {
        if (StringUtils.hasText(token)) {
            if (!jwtProvider.validateToken(token)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            Long userId = jwtProvider.getUserIdFromToken(token);
            User user = userRepository.findByIdWithMembershipsAndAdmins(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            UserRole role = user.getRole();
            List<Long> approvedCids = user.getCollegeMemberships().stream()
                    .map(cm -> cm.getCollege().getId())
                    .collect(Collectors.toList());
            List<Long> adminCids = role == UserRole.COLLEGE_ADMIN
                    ? user.getCollegeAdmins().stream()
                    .map(ca -> ca.getCollege().getId())
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.toString()));

            UserPrincipal principal = new UserPrincipal(userId, user.getEmail(), user.getName(), user.getStudentId(), approvedCids, adminCids, authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}