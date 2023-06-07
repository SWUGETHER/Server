package com.swugether.server.controller;

import com.swugether.server.base.constant.Code;
import com.swugether.server.base.dto.DataResponseDto;
import com.swugether.server.base.dto.ErrorResponseDto;
import com.swugether.server.base.dto.ResponseDto;
import com.swugether.server.exception.UnauthorizedAccessException;
import com.swugether.server.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.NoPermissionException;
import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@ResponseBody
@RequestMapping("/post")
@Slf4j
public class PostController {
    private PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ResponseDto> list(@RequestHeader("Authorization") String bearer_token,
                                            @RequestParam String order) {
        try {
            ArrayList<Map<String, Object>> list = postService.listService(bearer_token, order);

            return ResponseEntity.status(200).body(DataResponseDto.of(list));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(400).body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
        } catch (NoPermissionException | UnauthorizedAccessException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // 게시글 세부 조회
    @GetMapping(value = "/detail/{postId}")
    public ResponseEntity<ResponseDto> detail(@PathVariable Long postId,
                                              @RequestHeader("Authorization") String bearer_token) {
        try {
            Map<String, Object> content = postService.detailService(bearer_token, postId);

            return ResponseEntity.status(200).body(DataResponseDto.of(content));
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(400).body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
        } catch (NoPermissionException | UnauthorizedAccessException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ResponseDto> add(@RequestHeader("Authorization") String bearer_token,
                                           @RequestParam(value = "title") String title, @RequestParam(value = "content") String content,
                                           @RequestParam(value = "images") List<MultipartFile> images)
            throws IndexOutOfBoundsException, UnauthorizedAccessException, IllegalArgumentException {
        try {
            log.info(title);
            log.info(content);
            Map<String, Long> result = postService.addService(bearer_token, title, content, images);

            return ResponseEntity.status(200).body(DataResponseDto.of(result));
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(400).body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
        } catch (NoPermissionException | UnauthorizedAccessException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ResponseDto> update(@PathVariable Long postId,
                                              @RequestHeader("Authorization") String bearer_token,
                                              @RequestParam(value = "title", required = false) String title, @RequestParam(value = "content", required = false) String content,
                                              @RequestParam(value = "images", required = false) List<MultipartFile> images)
            throws IndexOutOfBoundsException, UnauthorizedAccessException, IllegalArgumentException, EntityNotFoundException {
        try {
            postService.updateService(bearer_token, postId, title, content, images);

            return ResponseEntity.status(200).body(ResponseDto.of(Code.OK));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(400).body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
        } catch (NoPermissionException | UnauthorizedAccessException | IndexOutOfBoundsException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(403)
                    .body(ErrorResponseDto.of(Code.FORBIDDEN, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseDto> delete(@PathVariable Long postId,
                                              @RequestHeader("Authorization") String bearer_token) throws EntityNotFoundException {
        try {
            postService.deleteService(bearer_token, postId);

            return ResponseEntity.status(200).body(ResponseDto.of(Code.OK));
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(400).body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
        } catch (NoPermissionException | UnauthorizedAccessException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401).body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            if (e.getMessage().equals("Not the writer.")) {
                return ResponseEntity.status(401).body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
            }

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // 게시글 좋아요 설정 및 취소
    @PostMapping("/like/{postId}")
    public ResponseEntity<ResponseDto> like(@PathVariable Long postId,
                                            @RequestHeader("Authorization") String bearer_token) {
        try {
            boolean isLiked = postService.likeService(bearer_token, postId);
            Map<String, Boolean> result = new HashMap<>();
            result.put("is_liked", isLiked);

            return ResponseEntity.status(200).body(DataResponseDto.of(result));
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(400).body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
        } catch (NoPermissionException | UnauthorizedAccessException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }

    // 좋아요한 게시글 목록 조회
    @GetMapping("/like")
    public ResponseEntity<ResponseDto> likeList(@RequestHeader("Authorization") String bearer_token) {
        try {
            ArrayList<Map<String, Object>> list = postService.likeListService(bearer_token);

            return ResponseEntity.status(200).body(DataResponseDto.of(list));
        } catch (NoPermissionException | UnauthorizedAccessException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }
}
