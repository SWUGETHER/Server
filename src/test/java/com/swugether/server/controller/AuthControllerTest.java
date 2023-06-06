package com.swugether.server.controller;

import com.swugether.server.db.dao.RefreshTokenRepository;
import com.swugether.server.db.dao.UserRepository;
import com.swugether.server.db.domain.RefreshTokenEntity;
import com.swugether.server.db.domain.UserEntity;
import com.swugether.server.service.AuthService;
import com.swugether.server.util.JwtProvider;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.Jwts.builder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtProvider jwtProvider;
    private final String login_url = "/user/login";
    private final String logout_url = "/user/logout";
    private final String leave_url = "/user/leave";
    private final String refresh_url = "/user/refresh";
    private String AUTHORIZATION;

    void setMockUser() {
        String email = "test@test.com";
        String nickname = "testnickname";
        UserEntity user = new UserEntity(email, nickname);
        Map<String, Object> tokens = authService.addUser(user);

        AUTHORIZATION = "Bearer " + tokens.get("accessToken").toString();
    }

    @Test
    void login_ok() throws Exception {
        // Insert id-token
        String ID_TOKEN = "";

        this.mockMvc.perform(post(login_url)
                        .header("id-token", ID_TOKEN))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void logout_ok() throws Exception {
        setMockUser();

        this.mockMvc.perform(post(logout_url)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void logout_invalid_user() throws Exception {
        String email = "test@test.com";
        String nickname = "testNickname";
        UserEntity user = new UserEntity(email, nickname);

        // 로그인
        Map<String, Object> loginData = authService.addUser(user);
        String ACCESS_TOKEN = loginData.get("accessToken").toString();

        // 미리 로그아웃
        authService.logoutService("Bearer " + ACCESS_TOKEN);

        this.mockMvc.perform(post(logout_url)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void logout_invalid_token() throws Exception {
        String ACCESS_TOKEN = "invalid";
        this.mockMvc.perform(post(logout_url)
                        .header("Authorization", ACCESS_TOKEN))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void leave_ok() {
        setMockUser();

        try {
            this.mockMvc.perform(post(leave_url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void refresh_ok() {
        // Set mock tokens
        String email = "test@test.com";
        String nickname = "test";
        UserEntity newUser = new UserEntity(email, nickname);
        UserEntity user = userRepository.findByEmail(newUser.getEmail())
                .orElse(userRepository.save(newUser));
        Long userId = user.getId();

        Map<String, Object> payloads = new HashMap<>();
        payloads.put("userId", userId);
        payloads.put("email", email);

        Date now = new Date();
        Date access_expiration = new Date(now.getTime() + Duration.ofNanos(2).toMillis()); // access token 만료시간: 2 ns
        Date refresh_expiration = new Date(now.getTime() + Duration.ofDays(7).toMillis()); // refresh token 만료시간: 7d

        String secretKey = System.getenv("JWT_SECRET_KEY");
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

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(userId, refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        try {
            this.mockMvc.perform(get(refresh_url)
                            .header("Authorization", "Bearer " + accessToken)
                            .header("Refresh", "Bearer " + refreshToken))
                    .andExpect(status().isOk())
                    .andDo(print());

            // Remove mock user
            refreshTokenRepository.deleteById(user.getId());
            userRepository.delete(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}