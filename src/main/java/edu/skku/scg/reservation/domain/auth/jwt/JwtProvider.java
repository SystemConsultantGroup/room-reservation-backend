package edu.skku.scg.reservation.domain.auth.jwt;

import edu.skku.scg.reservation.domain.auth.dto.AccessToken;
import edu.skku.scg.reservation.domain.auth.dto.RegisterToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
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
    private final long registerTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.accessToken.expiration}") long accessTokenExpiration,
            @Value("${jwt.registerToken.expiration}") long registerTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.registerTokenExpiration = registerTokenExpiration;
    }

    public String createAccessToken(Long userId, List<Long> managedUnitIds) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", TokenType.ACCESS)
                .claim("managedUnitIds", managedUnitIds)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public String createRegisterToken(String googleId, String email, String name) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + registerTokenExpiration);

        return Jwts.builder()
                .subject(googleId)
                .claim("type", TokenType.REGISTER)
                .claim("email", email)
                .claim("name", name)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public AccessToken parseAccessToken(String token) {
        Claims claims = getClaims(token);

        if (!TokenType.ACCESS.name().equals(claims.get("type", String.class))) {
            throw new SecurityException("Invalid token type");
        }

        List<?> rawUnitIds = claims.get("managedUnitIds", List.class);
        List<Long> managedUnitIds = (rawUnitIds != null) ?
                rawUnitIds.stream()
                        .map(obj -> Long.valueOf(obj.toString()))
                        .toList()
                : Collections.emptyList();

        return AccessToken.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .expiresAt(toLocalDateTime(claims.getExpiration()))
                .managedUnitIds(managedUnitIds)
                .build();
    }
    public RegisterToken parseRegisterToken(String token) {
        Claims claims = getClaims(token);

        if (TokenType.valueOf(claims.get("type", String.class)) != TokenType.REGISTER) {
            throw new SecurityException("Invalid token type");
        }

        return RegisterToken.builder()
                .googleId(claims.getSubject())
                .email(claims.get("email", String.class))
                .name(claims.get("name", String.class))
                .expiresAt(toLocalDateTime(claims.getExpiration()))
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