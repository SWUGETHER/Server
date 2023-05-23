package com.swugether.server.controller;

import com.swugether.server.base.dto.DataResponseDto;
import com.swugether.server.db.domain.User;
import com.swugether.server.util.GoogleOAuth;
import com.swugether.server.util.JwtProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
  GoogleOAuth googleOAuth = new GoogleOAuth();
  JwtProvider jwtProvider = new JwtProvider();

  // 로그인
  @GetMapping("/login")
  public DataResponseDto<Object> login(@RequestHeader("id-token") String id_token) {
    // id 유효성 확인 및 payload 추출
    User goolgeUser = googleOAuth.authenticate(id_token);
    Long userId = goolgeUser.getUserId();
    String email = goolgeUser.getEmail();

    // token 발행
    Map<String, Object> data = jwtProvider.createToken(userId, email);

    // 응답 전송
    return DataResponseDto.of(data);
  }

  // 로그아웃
}
