package com.swugether.server.controller;

import com.swugether.server.domain.Auth.application.AuthService;
import com.swugether.server.domain.Auth.domain.UserEntity;
import com.swugether.server.domain.Post.application.PostService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.NoPermissionException;
import java.io.FileInputStream;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class MypageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private PostService postService;
    private String ACCESS_TOKEN;

    @BeforeEach
    void setMockUser() {
        String email = "test@test.com";
        String nickname = "testnickname";
        UserEntity user = new UserEntity(email, nickname);
        Map<String, Object> tokens = authService.addUser(user);

        ACCESS_TOKEN = tokens.get("accessToken").toString();
    }

    @AfterEach
    void removeMockUser() throws NoPermissionException {
        authService.leaveService("Bearer " + ACCESS_TOKEN);
    }

    @Test
    void postList() throws FileSystemException, NoPermissionException {
        String authorization = "Bearer " + ACCESS_TOKEN;
        Long postId;

        // dummy post 작성
        try {
            String title = "test1_title";
            String content = "test1_content";

            String filePath = "src/test/resources/images/test1.jpg";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            MockMultipartFile image1 = new MockMultipartFile("image1", "test1.jpg", "jpg", fileInputStream);

            filePath = "src/test/resources/images/test2.jpg";
            FileInputStream fileInputStream2 = new FileInputStream(filePath);
            MockMultipartFile image2 = new MockMultipartFile("image2", "test2.jpg", "jpg", fileInputStream2);

            filePath = "src/test/resources/images/test3.jpg";
            FileInputStream fileInputStream3 = new FileInputStream(filePath);
            MockMultipartFile image3 = new MockMultipartFile("image3", "test3.jpg", "jpg", fileInputStream3);

            MultipartFile[] images = {image1, image2, image3};

            Map<String, Long> result = postService.addService(authorization, title, content, List.of(images));
            postId = result.get("post_id");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        // post list 조회
        try {
            // URL
            String post_url = "/mypage/post";
            this.mockMvc.perform(get(post_url)
                            .header("Authorization", authorization))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // dummy post 삭제
            postService.deleteService(authorization, postId);
        }
    }
}