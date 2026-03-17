package edu.skku.scg.reservation.domain.auth.jwt;

import edu.skku.scg.reservation.domain.user.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String createToken(
            Long userId,
            UserRole role,
            List<Long> approvedCids,
            List<Long> adminCids) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("approvedCids", approvedCids)
                .claim("adminCids", adminCids)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String updateToken(
            String token,
            UserRole role,
            List<Long> approvedCids,
            List<Long> adminCids) {
        return Jwts.builder()
                .subject(getUserIdFromToken(token))
                .claim("role", role)
                .claim("approvedCids", approvedCids)
                .claim("adminCids", adminCids)
                .issuedAt(new Date())
                .expiration(getExpirationFromToken(token))
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public List<Long> getApprovedCidsFromToken(String token) {
        List<?> list = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("approvedCids", List.class);

        if (list == null) {
            return List.of();
        }

        return list.stream()
                .map(item -> ((Number) item).longValue())
                .toList();
    }

    public List<Long> getAdminCidsFromToken(String token) {
        List<?> list = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("adminCids", List.class);

        if (list == null) {
            return List.of();
        }

        return list.stream()
                .map(item -> ((Number) item).longValue())
                .toList();
    }

    public Date getExpirationFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload().getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}