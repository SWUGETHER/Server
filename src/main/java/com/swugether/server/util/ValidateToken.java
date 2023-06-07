package com.swugether.server.util;

import com.swugether.server.db.dao.RefreshTokenRepository;
import com.swugether.server.db.dao.UserRepository;
import com.swugether.server.db.domain.UserEntity;
import com.swugether.server.exception.UnauthorizedAccessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NoPermissionException;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateToken {
  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Autowired
  public ValidateToken(JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository) {
    this.jwtProvider = jwtProvider;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
  }

  public UserEntity validateAuthorization(@NotNull String authorization)
      throws IndexOutOfBoundsException, UnauthorizedAccessException, NoPermissionException {
    Map<String, Object> payload = new HashMap<>();
    Long userId;

    // 헤더에서 token 추출
    String accessToken = authorization.split("Bearer ")[1];

    try {
      // 토큰 유효성 검사 및 유저 정보 추출
      payload = jwtProvider.verifyJWT(accessToken);
      userId = ((Number) payload.get("userId")).longValue();
    } catch (InvalidClaimException | ExpiredJwtException e) {
      throw new UnauthorizedAccessException(e.getMessage());
    }

    // 로그인 여부 검사
    boolean isRefreshTokenExist = refreshTokenRepository.existsById(userId);

    if (!isRefreshTokenExist) {
      throw new NoPermissionException("User is logged out");
    }

    return userRepository.findById(userId)
        .orElse(null);
  }
}
