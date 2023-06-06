package com.swugether.server.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;

@Component
public class JwtProvider {
    private final String secretKey = System.getenv("JWT_SECRET_KEY");

    // 토큰 발행
    public Map<String, Object> createToken(Long userId, String email) {
        Map<String, Object> payloads = new HashMap<>();
        payloads.put("userId", userId);
        payloads.put("email", email);

        Date now = new Date();
        Date access_expiration = new Date(now.getTime() + Duration.ofHours(2).toMillis()); // access token 만료시간: 2h
        Date refresh_expiration = new Date(now.getTime() + Duration.ofDays(7).toMillis()); // refresh token 만료시간: 7d

        String accessToken = builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(payloads)
                .setIssuer("admin")
                .setIssuedAt(now)
                .setExpiration(access_expiration)
                .setSubject("accessToken")
                .signWith(SignatureAlgorithm.HS256,
                        Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .compact();
        String refreshToken = builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(payloads)
                .setIssuer("admin")
                .setIssuedAt(now)
                .setExpiration(refresh_expiration)
                .setSubject("refreshToken")
                .signWith(SignatureAlgorithm.HS256,
                        Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .compact();

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);

        return data;
    }

    // 토큰 검증
    public Map<String, Object> verifyJWT(String jwt)
            throws InvalidClaimException, ExpiredJwtException {
        return parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(jwt)
                .getBody();
    }
}
