package com.swugether.server.controller;

import com.swugether.server.base.constant.Code;
import com.swugether.server.base.dto.DataResponseDto;
import com.swugether.server.base.dto.ErrorResponseDto;
import com.swugether.server.base.dto.ResponseDto;
import com.swugether.server.service.AuthService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
  private AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  // 로그인
  @GetMapping("/login")
  public ResponseDto login(@RequestHeader("id-token") String id_token) {
    try {
      Map<String, Object> data = authService.loginService(id_token);

      return DataResponseDto.of(data);
    } catch (GeneralSecurityException | IOException e) {
      log.error(e.getMessage());

      return ErrorResponseDto.of(Code.UNAUTHORIZED, "Invalid id token.");
    } catch (Exception e) {
      log.error(e.getMessage());

      return ErrorResponseDto.of(Code.INTERNAL_ERROR, "Server error.");
    }
  }

  // 로그아웃
  @GetMapping("/logout")
  public ResponseDto logout(@RequestHeader("Authorization") String header) {
    return ResponseDto.of(Code.OK);
  }

  // 회원탈퇴
  @GetMapping("/leave")
  public ResponseDto leave(@RequestHeader("Authorization") String bearer_token) {
    return ResponseDto.of(Code.OK);
  }

  // AccessToken 재발급
  @GetMapping("/refresh")
  public ResponseDto leave(@RequestHeader("Authorization") String bearer_token,
      @RequestHeader("Refresh") String bearer_refresh) {
    return ResponseDto.of(Code.OK);
  }
}
