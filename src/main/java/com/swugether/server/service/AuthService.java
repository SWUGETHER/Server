package com.swugether.server.service;

import com.swugether.server.db.dao.RefreshTokenRepository;
import com.swugether.server.db.dao.UserRepository;
import com.swugether.server.db.domain.RefreshTokenEntity;
import com.swugether.server.db.domain.UserEntity;
import com.swugether.server.exception.UnauthorizedAccessException;
import com.swugether.server.util.GoogleOAuth;
import com.swugether.server.util.JwtProvider;
import com.swugether.server.util.ValidateToken;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.naming.NoPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final GoogleOAuth googleOAuth;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final ValidateToken validateToken;

  @Autowired
  public AuthService(GoogleOAuth googleOAuth, JwtProvider jwtProvider,
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository, ValidateToken validateToken) {
    this.googleOAuth = googleOAuth;
    this.jwtProvider = jwtProvider;
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.validateToken = validateToken;
  }

  // 토큰 발행
  public Map<String, Object> generateTokens(Long userId, String email) {
    //token 발행
    Map<String, Object> tokens = jwtProvider.createToken(userId, email);

    // redis에 refreshToken 저장
    RefreshTokenEntity refreshToken = new RefreshTokenEntity(userId,
        (String) tokens.get("refreshToken"));
    refreshTokenRepository.save(refreshToken);

    return tokens;
  }

  // 유저 추가
  public Map<String, Object> addUser(UserEntity googleUser) {
    // DB 조회 (이미 회원인 경우 / 새로운 회원인 경우)
    UserEntity user = userRepository.findByEmail(googleUser.getEmail())
        .orElse(userRepository.save(googleUser));
    Long userId = user.getId();

    // token
    Map<String, Object> tokens = generateTokens(userId, googleUser.getEmail());

    // 응답 전송
    Map<String, Object> responseData = new LinkedHashMap<>();
    responseData.put("userId", userId);
    responseData.putAll(tokens);

    return responseData;
  }

  // 로그인
  public Map<String, Object> loginService(String id_token)
      throws GeneralSecurityException, IOException {

    // token 유효성 확인 및 payload 추출
    UserEntity googleUser = googleOAuth.authenticate(id_token);

    if (googleUser == null) {
      throw new GeneralSecurityException("Invalid id token.");
    }

    return addUser(googleUser);
  }

  // 로그아웃
  public void logoutService(String authorization)
      throws IndexOutOfBoundsException, UnauthorizedAccessException, NoPermissionException, EmptyResultDataAccessException {
    // 토큰 유효성 검사 및 유저 정보 추출
    UserEntity user = validateToken.validateAuthorization(authorization);

    // user가 존재하지 않을 경우
    if (user == null) {
      throw new EmptyResultDataAccessException(1);
    }

    // redis 내 refresh token 데이터 삭제
    refreshTokenRepository.deleteById(user.getId());
  }

  // 회원탈퇴
  public void leaveService(String authorization)
      throws IndexOutOfBoundsException, UnauthorizedAccessException, NoPermissionException, EmptyResultDataAccessException {
    // 토큰 유효성 검사 및 유저 정보 추출
    UserEntity user = validateToken.validateAuthorization(authorization);

    // user가 존재하지 않을 경우
    if (user == null) {
      throw new EmptyResultDataAccessException(1);
    }

    // redis 내 refresh token 데이터 삭제
    refreshTokenRepository.deleteById(user.getId());

    // db 내 user 데이터 삭제
    userRepository.delete(user);
  }

  // access token 재발급
  public Map<String, Object> refreshService(String authorization, String refresh)
      throws NoPermissionException, IndexOutOfBoundsException, UnauthorizedAccessException {
    boolean isAllowed;

    // accessToken 만료 검사
    try {
      validateToken.validateAuthorization(authorization);
      isAllowed = false;
    } catch (ExpiredJwtException e) {
      isAllowed = true;
    }

    if (!isAllowed) {
      throw new NoPermissionException("Access token still valid.");
    }

    // refreshToken 유효성 검사
    Map<String, Object> payload = jwtProvider.verifyJWT(refresh);
    Long userId = ((Number) payload.get("userId")).longValue();
    String email = payload.get("email").toString();

    // token 재발급
    return generateTokens(userId, email);
  }
}
