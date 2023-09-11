package com.swugether.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swugether.server.domain.Auth.application.AuthService;
import com.swugether.server.domain.Auth.domain.UserEntity;
import com.swugether.server.domain.Auth.domain.UserRepository;
import com.swugether.server.domain.Post.application.PostService;
import com.swugether.server.domain.Post.domain.ContentEntity;
import com.swugether.server.domain.Post.domain.ContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.NoPermissionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private PostService postService;
    private String AUTHORIZATION;
    private Long userId;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContentRepository contentRepository;

    @BeforeEach
    void setMockUser() {
        String email = "test@test.com";
        String nickname = "testnickname";
        UserEntity user = new UserEntity(email, nickname);
        Map<String, Object> tokens = authService.addUser(user);

        AUTHORIZATION = "Bearer " + tokens.get("accessToken").toString();
        userId = (Long) tokens.get("userId");
    }

    @AfterEach
    void removeMockUser() throws NoPermissionException {
        authService.leaveService(AUTHORIZATION);
    }

    long addDummyContent(String title) {
        try {
            String content = title + "_content";

            String filePath = "src/test/resources/images/test1.jpg";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            MockMultipartFile image1 = new MockMultipartFile("images", "test1.jpg", "jpg", fileInputStream);

            filePath = "src/test/resources/images/test2.jpg";
            FileInputStream fileInputStream2 = new FileInputStream(filePath);
            MockMultipartFile image2 = new MockMultipartFile("images", "test2.jpg", "jpg", fileInputStream2);

            filePath = "src/test/resources/images/test3.jpg";
            FileInputStream fileInputStream3 = new FileInputStream(filePath);
            MockMultipartFile image3 = new MockMultipartFile("images", "test3.jpg", "jpg", fileInputStream3);

            MultipartFile[] images = {image1, image2, image3};

            Map<String, Long> result = postService.addService(AUTHORIZATION, title, content, List.of(images));

            return result.get("post_id");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void deleteDummyContent() {
        try {
            UserEntity user = userRepository.findById(userId).orElse(null);
            List<ContentEntity> posts = contentRepository.findAllByUserOrderByCreatedAtDesc(user);
            for (ContentEntity post : posts) {
                postService.deleteImages(post);
                contentRepository.deleteById(post.getId());
            }
        } catch (FileSystemException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void list() {
        // dummy data 추가
        for (int i = 0; i < 3; i++) {
            addDummyContent("title_" + i);
        }

        String[] orderList = {"recent", "oldest", "like"};

        try {
            String url = "/post";
            for (String order : orderList) {
                this.mockMvc.perform(get(url)
                                .header("Authorization", AUTHORIZATION)
                                .param("order", order))
                        .andExpect(status().isOk())
                        .andDo(print());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        // dummy data 삭제
        deleteDummyContent();
    }

    @Test
    void detail() {
        // dummy data 추가
        long post_id = addDummyContent("test_title");
        String url = "/post/detail/";

        try {
            ResultActions result = this.mockMvc.perform(get(url + post_id)
                    .header("Authorization", AUTHORIZATION)
                    .accept(MediaType.APPLICATION_JSON));
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("data.post_id").value(post_id))
                    .andDo(print());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        // dummy data 삭제
        deleteDummyContent();
    }

    @Test
    void add() throws Exception {
        String url = "/post";
        String title = "test_title";
        String content = "test_content";
        MockMultipartFile[] images = new MockMultipartFile[3];
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String filePath = "src/test/resources/images/test1.jpg";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            MockMultipartFile image1 = new MockMultipartFile("images", "test1.jpg", "jpg", fileInputStream);
            images[0] = image1;

            filePath = "src/test/resources/images/test2.jpg";
            FileInputStream fileInputStream2 = new FileInputStream(filePath);
            MockMultipartFile image2 = new MockMultipartFile("images", "test2.jpg", "jpg", fileInputStream2);
            images[1] = image2;

            filePath = "src/test/resources/images/test3.jpg";
            FileInputStream fileInputStream3 = new FileInputStream(filePath);
            MockMultipartFile image3 = new MockMultipartFile("images", "test3.jpg", "jpg", fileInputStream3);
            images[2] = image3;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            this.mockMvc.perform(multipart(url)
                            .file(images[0])
                            .file(images[1])
                            .file(images[2])
                            .param("title", title)
                            .param("content", content)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn();

            // 게시글 삭제
            deleteDummyContent();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Test
    void update() {
        // dummy data 생성
        long post_id;
        try {
            String title = "test_title";
            String content = "test_content";

            String filePath = "src/test/resources/images/test1.jpg";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            MockMultipartFile image1 = new MockMultipartFile("images", "test1.jpg", "jpg", fileInputStream);

            MultipartFile[] images = {image1};

            Map<String, Long> result = postService.addService(AUTHORIZATION, title, content, List.of(images));

            post_id = result.get("post_id");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        String url = "/post/" + post_id;

        try {
            // 수정 전 detail 조회
            this.mockMvc.perform(get("/post/detail/" + post_id)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // 수정
            String title = "edited_test_title";
            String content = "edited_test_content";

            String filePath = "src/test/resources/images/test2.jpg";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            MockMultipartFile image = new MockMultipartFile("images", "test2.jpg", "jpg", fileInputStream);

            MockMultipartFile[] images = {image};

            this.mockMvc.perform(multipart(HttpMethod.PATCH, url)
                            .file(images[0])
                            .param("title", title)
                            .param("content", content)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // 수정 후 detail 조회
            this.mockMvc.perform(get("/post/detail/" + post_id)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        // dummy data 삭제
        deleteDummyContent();
    }

    @Test
    void deletePost() {
        // dummy data 생성
        long post_id = addDummyContent("test_title");
        String url = "/post/" + post_id;

        try {
            // 삭제 전 post 목록 조회
            this.mockMvc.perform(get("/post")
                            .header("Authorization", AUTHORIZATION)
                            .param("order", "recent"))
                    .andDo(print());

            // post 삭제
            this.mockMvc.perform(delete(url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // 삭제 후 post 목록 조회
            this.mockMvc.perform(get("/post")
                            .header("Authorization", AUTHORIZATION)
                            .param("order", "recent"))
                    .andDo(print());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    void like() {
        // dummy data 생성
        long post_id = addDummyContent("test_title");
        String url = "/post/like/" + post_id;

        try {
            // like 전 post 조회
            this.mockMvc.perform(get("/post")
                            .header("Authorization", AUTHORIZATION)
                            .param("order", "recent"))
                    .andDo(print());

            // like
            this.mockMvc.perform(post(url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // like 후 post 조회
            this.mockMvc.perform(get("/post")
                            .header("Authorization", AUTHORIZATION)
                            .param("order", "recent"))
                    .andDo(print());

            // like 취소
            this.mockMvc.perform(post(url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // like 취소 후 post 조회
            this.mockMvc.perform(get("/post")
                            .header("Authorization", AUTHORIZATION)
                            .param("order", "recent"))
                    .andDo(print());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        // dummy data 삭제
        deleteDummyContent();
    }

    @Test
    void likeList() {
        // dummy data 생성
        long post_id = addDummyContent("test_title");
        String url = "/post/like";

        try {
            // like 전 like한 post 조회
            this.mockMvc.perform(get(url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // like
            this.mockMvc.perform(post("/post/like/" + post_id)
                            .header("Authorization", AUTHORIZATION))
                    .andDo(print());

            // like 후 like한 post 조회
            this.mockMvc.perform(get(url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());

            // like 취소
            this.mockMvc.perform(post("/post/like/" + post_id)
                            .header("Authorization", AUTHORIZATION))
                    .andDo(print());

            // like 취소 후 like한 post 조회
            this.mockMvc.perform(get(url)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        // dummy data 삭제
        deleteDummyContent();
    }
}