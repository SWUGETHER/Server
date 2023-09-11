package com.swugether.server.domain.Auth.api;

import com.swugether.server.domain.Auth.application.AuthService;
import com.swugether.server.global.base.constant.Code;
import com.swugether.server.global.base.dto.DataResponseDto;
import com.swugether.server.global.base.dto.ErrorResponseDto;
import com.swugether.server.global.base.dto.ResponseDto;
import com.swugether.server.global.exception.UnauthorizedAccessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.NoPermissionException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@ResponseBody
@RequestMapping("/user")
@Slf4j
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestHeader("id-token") String id_token) {
        try {
            Map<String, Object> data = authService.loginService(id_token);

            return ResponseEntity.status(200).body(DataResponseDto.of(data));
        } catch (GeneralSecurityException | IOException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, "Invalid id token."));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, "Server error."));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto> logout(@RequestHeader("Authorization") String bearer_token)
            throws IndexOutOfBoundsException, UnauthorizedAccessException, EmptyResultDataAccessException {
        try {
            authService.logoutService(bearer_token);

            return ResponseEntity.ok(ResponseDto.of(Code.OK));
        } catch (IndexOutOfBoundsException | InvalidClaimException | ExpiredJwtException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, "Invalid Access token."));
        } catch (UnauthorizedAccessException | EmptyResultDataAccessException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (NoPermissionException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(403)
                    .body(ErrorResponseDto.of(Code.FORBIDDEN, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, "Internal server error"));
        }
    }

    // 회원탈퇴
    @PostMapping("/leave")
    public ResponseEntity<ResponseDto> leave(@RequestHeader("Authorization") String bearer_token) {
        try {
            authService.leaveService(bearer_token);

            return ResponseEntity.ok(ResponseDto.of(Code.OK));
        } catch (IndexOutOfBoundsException | InvalidClaimException | ExpiredJwtException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, "Invalid Access token."));
        } catch (UnauthorizedAccessException | EmptyResultDataAccessException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, "Internal server error"));
        }
    }

    // AccessToken 재발급
    @GetMapping("/refresh")
    public ResponseEntity<ResponseDto> refresh(@RequestHeader("Authorization") String bearer_token,
                                               @RequestHeader("Refresh") String bearer_refresh)
            throws IndexOutOfBoundsException, InvalidClaimException, ExpiredJwtException, UnauthorizedAccessException {
        try {
            return ResponseEntity.status(200)
                    .body(DataResponseDto.of(authService.refreshService(bearer_token, bearer_refresh)));
        } catch (NoPermissionException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(403)
                    .body(ErrorResponseDto.of(Code.FORBIDDEN, e.getMessage()));
        } catch (IndexOutOfBoundsException | InvalidClaimException | ExpiredJwtException |
                 UnauthorizedAccessException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, "Server error."));
        }
    }
}
