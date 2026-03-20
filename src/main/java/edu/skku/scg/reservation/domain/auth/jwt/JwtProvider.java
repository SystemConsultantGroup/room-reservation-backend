package edu.skku.scg.reservation.domain.auth.jwt;

import edu.skku.scg.reservation.domain.auth.dto.AccessToken;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long accessTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String createAccessToken(Long userId, UserType type, List<Long> managingUnitIds) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", type.name())
                .claim("managingUnitIds", managingUnitIds)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public AccessToken parseAccessToken(String token) {
        Claims claims = getClaims(token);

        UserType type = UserType.valueOf(claims.get("type", String.class));

        List<?> rawUnitIds = claims.get("managingUnitIds", List.class);
        List<Long> managingUnitIds = (rawUnitIds != null) ?
                rawUnitIds.stream()
                        .map(obj -> Long.valueOf(obj.toString()))
                        .toList()
                : Collections.emptyList();

        return AccessToken.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .type(type)
                .expiresAt(toLocalDateTime(claims.getExpiration()))
                .managingUnitIds(managingUnitIds)
                .build();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}