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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final boolean cookieSecure;
    private final HandlerExceptionResolver exceptionResolver;

    JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserRepository userRepository,
            @Value("${cookie.secure}") boolean cookieSecure,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.cookieSecure = cookieSecure;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseCookieToken(request);

        try {
            if (StringUtils.hasText(token)) {
                if (!jwtProvider.validateToken(token)) {
                    throw new BusinessException(ErrorCode.INVALID_TOKEN);
                }

                String userId = jwtProvider.getUserIdFromToken(token);
                User user = userRepository.findByIdWithMembershipsAndAdmins(Long.parseLong(userId))
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                UserRole role = user.getRole();
                List<Long> approvedCids = user.getCollegeMemberships().stream()
                        .map(cm -> cm.getCollege().getId())
                        .collect(Collectors.toList());
                List<Long> adminCids = user.getCollegeAdmins().stream()
                        .map(ca -> ca.getCollege().getId())
                        .collect(Collectors.toList());
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.toString()));

                if (isPrivilegeChanged(token, role.toString(), approvedCids, adminCids)) {
                    updateCookie(response, token, role, approvedCids, adminCids);
                }

                UserPrincipal principal = new UserPrincipal(userId, approvedCids, adminCids, authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();
            exceptionResolver.resolveException(request, response, null, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String parseCookieToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, AuthConstants.ACCESS_TOKEN_COOKIE_NAME);
        return (cookie != null) ? cookie.getValue() : null;
    }

    private boolean isPrivilegeChanged(String token, String dbRole, List<Long> dbApprovedCids, List<Long> dbAdminCids) {
        String tokenRole = jwtProvider.getRoleFromToken(token);
        List<Long> tokenApprovedCids = jwtProvider.getApprovedCidsFromToken(token);
        List<Long> tokenAdminCids = jwtProvider.getAdminCidsFromToken(token);

        if (!dbRole.equals(tokenRole)) return true;
        if (!isListEqualIgnoreOrder(dbApprovedCids, tokenApprovedCids)) return true;
        if (!isListEqualIgnoreOrder(dbAdminCids, tokenAdminCids)) return true;

        return false;
    }

    private void updateCookie(@NonNull HttpServletResponse response, String token, UserRole role, List<Long> approvedCids, List<Long> adminCids) {
        Date expiration = jwtProvider.getExpirationFromToken(token);
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();

        ResponseCookie cookie = ResponseCookie.from(
                        AuthConstants.ACCESS_TOKEN_COOKIE_NAME,
                        jwtProvider.updateToken(token, role, approvedCids, adminCids))
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofMillis(remainingMillis))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private boolean isListEqualIgnoreOrder(List<Long> list1, List<Long> list2) {
        if (list1 == null) list1 = List.of();
        if (list2 == null) list2 = List.of();

        if (list1.size() != list2.size()) return false;

        return new java.util.HashSet<>(list1).containsAll(list2);
    }
}