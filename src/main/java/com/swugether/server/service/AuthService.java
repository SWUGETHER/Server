package com.swugether.server.service;

import com.swugether.server.db.dao.RefreshTokenRepository;
import com.swugether.server.db.dao.UserRepository;
import com.swugether.server.db.domain.RefreshTokenEntity;
import com.swugether.server.db.domain.UserEntity;
import com.swugether.server.util.GoogleOAuth;
import com.swugether.server.util.JwtProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final GoogleOAuth googleOAuth;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  @Autowired
  public AuthService(GoogleOAuth googleOAuth, JwtProvider jwtProvider,
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository) {
    this.googleOAuth = googleOAuth;
    this.jwtProvider = jwtProvider;
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public Map<String, Object> loginService(String id_token)
      throws GeneralSecurityException, IOException {

    // token 유효성 확인 및 payload 추출
    UserEntity googleUser = googleOAuth.authenticate(id_token);

    if (googleUser == null) {
      throw new GeneralSecurityException("Invalid id token.");
    }

    String email = googleUser.getEmail();

    // DB 조회 (이미 회원인 경우 / 새로운 회원인 경우)
    UserEntity user = userRepository.findByEmail(email)
        .orElse(userRepository.save(googleUser));
    Long userId = user.getId();

    // token
    Map<String, Object> tokens = generateTokens(userId, email);

    // 응답 전송
    Map<String, Object> responseData = new LinkedHashMap<>();
    responseData.put("userId", userId);
    responseData.putAll(tokens);

    return responseData;
  }

  public Map<String, Object> generateTokens(Long userId, String email) {
    //token 발행
    Map<String, Object> tokens = jwtProvider.createToken(userId, email);

    // redis에 refreshToken 저장
    RefreshTokenEntity refreshToken = new RefreshTokenEntity(userId,
        (String) tokens.get("refreshToken"));
    refreshTokenRepository.save(refreshToken);

    return tokens;
  }
}
