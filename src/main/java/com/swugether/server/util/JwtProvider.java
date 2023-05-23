package com.swugether.server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
  private String secretKey = System.getenv("JWT_SECRET_KEY");

  // 토큰 발행
  public Map<String, Object> createToken(Long userId, String email) {
    Map<String, Object> payloads = new HashMap<>();
    payloads.put("userId", userId);
    payloads.put("email", email);

    Date now = new Date();
    Date expiration = new Date(now.getTime() + Duration.ofHours(2).toMillis()); // 만료시간: 2h

    Map<String, Object> data = new HashMap<>();
    String accessToken = Jwts.builder()
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
        .setClaims(payloads)
        .setIssuer("admin")
        .setIssuedAt(now)
        .setExpiration(expiration)
        .setSubject("accessToken")
        .signWith(SignatureAlgorithm.HS256,
            Base64.getEncoder().encodeToString(secretKey.getBytes()))
        .compact();
    String refreshToken = Jwts.builder()
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
        .setClaims(payloads)
        .setIssuer("admin")
        .setIssuedAt(now)
        .setExpiration(expiration)
        .setSubject("refreshToken")
        .signWith(SignatureAlgorithm.HS256,
            Base64.getEncoder().encodeToString(secretKey.getBytes()))
        .compact();
    data.put("accessToken", accessToken);
    data.put("refreshToken", refreshToken);

    return data;
  }

  // 토큰 검증
  public Map<String, Object> verifyJWT(String jwt) throws UnsupportedEncodingException {
    Map<String, Object> claimMap = null;

    try {
      Claims claims = Jwts.parser()
          .setSigningKey(secretKey.getBytes())
          .parseClaimsJws(jwt)
          .getBody();

      claimMap = claims;
    } catch (ExpiredJwtException e) {
      System.out.println(e);
    } catch (Exception e) {
      System.out.println(e);
    }

    return claimMap;
  }
}
