package com.swugether.server.domain.Post.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swugether.server.domain.Auth.domain.UserEntity;
import com.swugether.server.domain.Post.domain.*;
import com.swugether.server.domain.Post.dto.PostDto;
import com.swugether.server.domain.Post.dto.SavedPostDto;
import com.swugether.server.global.exception.UnauthorizedAccessException;
import com.swugether.server.global.util.PostDtoProvider;
import com.swugether.server.global.util.ValidateToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.NoPermissionException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PostService {
    private final ValidateToken validateToken;
    private final PostDtoProvider postDtoProvider;
    private final ContentRepository contentRepository;
    private final ImageRepository imageRepository;
    private final LikedRepository likedRepository;
    private final ObjectMapper objectMapper;
    private final String ImagePath = System.getenv("FILE_UPLOAD_BASE_PATH");

    @Autowired
    public PostService(ValidateToken validateToken, PostDtoProvider postDtoProvider,
                       ContentRepository contentRepository, ImageRepository imageRepository,
                       LikedRepository likedRepository, ObjectMapper objectMapper, EntityManager entityManager) {
        this.validateToken = validateToken;
        this.postDtoProvider = postDtoProvider;
        this.contentRepository = contentRepository;
        this.imageRepository = imageRepository;
        this.likedRepository = likedRepository;
        this.objectMapper = objectMapper;
    }

    // 게시글 작성 값 유효성 검사
    public void isPostValueValid(String str, boolean isTitle) throws IllegalArgumentException {
        boolean isNotValid = true;

        // 공백 검사
        isNotValid = (str == null) || str.isBlank();

        // title 길이 검사
        if (isTitle) {
            assert str != null;
            isNotValid = (str.length() == 0) || (str.length() > 100);
        }

        if (isNotValid) {
            throw new IllegalArgumentException("Invalid value.");
        }
    }

    // 이미지 저장
    public void saveImages(ContentEntity post, List<MultipartFile> images) throws IOException {
        for (MultipartFile file : images) {
            ImageEntity image = new ImageEntity(post, null);
            Long image_id = imageRepository.save(image).getId();
            String file_name = image_id + "_" + file.getOriginalFilename();
            String file_path = "/" + post.getId() + "_" + file_name;
            Path imagePath = Paths.get(ImagePath + file_path);

            file.transferTo(new File(ImagePath + file_path));

            imageRepository.updateImagePath(file_path, image_id);
        }
    }

    // 이미지 삭제
    public void deleteImages(ContentEntity post) throws FileSystemException {
        List<ImageEntity> images = imageRepository.findAllByPost(post);

        for (ImageEntity image : images) {
            // 파일 삭제
            try {
                File file = new File(ImagePath + image.getImagePath());
                boolean result = file.delete();
                if (result) {
                    log.info("File deleted: " + image.getImagePath());
                } else {
                    log.error("File deletion failed: " + image.getImagePath());
                }
            } catch (Exception e) {
                throw new FileSystemException("Deleted image failed.");
            }

            // 데이터 삭제
            imageRepository.deleteById(image.getId());
        }
    }

    // 게시글 목록 조회
    public ArrayList<Map<String, Object>> listService(String authorization, String order)
            throws IllegalStateException, UnauthorizedAccessException, NoPermissionException {
        List<ContentEntity> contents;
        ArrayList<Map<String, Object>> result = new ArrayList<>();

        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        // 게시글 데이터 조회
        contents = switch (order) {
            case "recent" -> contentRepository.findAllByOrderByCreatedAtDesc();
            case "oldest" -> contentRepository.findAllByOrderByCreatedAtAsc();
            case "like" -> contentRepository.findAllByOrderByLikeCountDesc();
            default -> throw new IllegalStateException("Unexpected value: " + order);
        };

        // 데이터 정제
        for (ContentEntity post : contents) {
            result.add(objectMapper.convertValue(postDtoProvider.getPostItemDto(post, user), Map.class));
        }

        return result;
    }

    // 좋아요한 게시글 목록 조회
    public ArrayList<Map<String, Object>> likeListService(String authorization)
            throws UnauthorizedAccessException, NoPermissionException {
        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        // 게시글 데이터 조회
        List<LikedEntity> likes = likedRepository.findAllByUser(user);
        List<ContentEntity> contents = new ArrayList<>();

        for (LikedEntity liked : likes) {
            ContentEntity content = liked.getPost();
            contents.add(content);
        }

        // 데이터 정제
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (ContentEntity post : contents) {
            result.add(objectMapper.convertValue(postDtoProvider.getPostItemDto(post, user), Map.class));
        }

        return result;
    }

    // 게시글 세부 조회
    public Map<String, Object> detailService(String authorization, Long postId)
            throws IndexOutOfBoundsException, UnauthorizedAccessException, NoPermissionException, EntityNotFoundException {
        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        ContentEntity post = contentRepository.findById(postId)
                .orElse(null);

        if (post == null) {
            throw new EntityNotFoundException();
        }

        PostDto postDto = postDtoProvider.getPostDto(post, user);
        return objectMapper.convertValue(postDto, Map.class);
    }

    // 게시글 작성
    public Map<String, Long> addService(String authorization, String title, String content,
                                        List<MultipartFile> images)
            throws IndexOutOfBoundsException, UnauthorizedAccessException, NoPermissionException, IllegalArgumentException, IOException {
        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        // 작성 값 유효성 검사
        isPostValueValid(title, true);
        isPostValueValid(content, false);

        // 게시글 데이터 저장
        ContentEntity post = new ContentEntity(user, title, content);
        Long postId = contentRepository.save(post).getId();

        // 이미지 데이터 저장
        saveImages(post, images);

        // dto
        SavedPostDto savedPostDto = SavedPostDto.builder()
                .post_id(postId)
                .build();

        return objectMapper.convertValue(savedPostDto, Map.class);
    }

    // 게시글 수정
    public void updateService(String authorization, Long postId, String title,
                              String content, List<MultipartFile> images)
            throws IndexOutOfBoundsException, UnauthorizedAccessException, NoPermissionException, IllegalArgumentException, EntityNotFoundException, IOException {
        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        // 게시글 조회
        ContentEntity post = contentRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new EntityNotFoundException();
        }

        if (!post.getUser().equals(user)) {
            throw new AccessDeniedException("Not the writer.");
        }

        if (title != null) {
            // 값 유효성 검사
            isPostValueValid(title, true);
            // 데이터 업데이트
            post.setTitle(title);
        }

        if (content != null) {
            // 값 유효성 검사
            isPostValueValid(content, true);
            // 데이터 업데이트
            post.setContent(content);
        }

        if (images != null) {
            // 이미지 데이터 수정
            deleteImages(post);

            // 이미지 데이터 저장
            saveImages(post, images);
        }
    }

    // 게시글 삭제
    public void deleteService(String authorization, Long postId)
            throws NoPermissionException, EntityNotFoundException, FileSystemException {
        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        // 게시글 조회
        ContentEntity post = contentRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new EntityNotFoundException();
        }

        // 게시글 작성자 확인
        if (!Objects.equals(user.getId(), post.getUser().getId())) {
            throw new AccessDeniedException("Not the writer.");
        }

        // 이미지 삭제
        deleteImages(post);

        // 게시글 삭제
        contentRepository.deleteById(postId);
    }

    // 게시글 좋아요 관리
    public Boolean likeService(String authorization, Long postId)
            throws NoPermissionException, EntityNotFoundException {
        // 토큰 유효성 검사 및 유저 정보 추출
        UserEntity user = validateToken.validateAuthorization(authorization);

        // 게시글 조회
        ContentEntity post = contentRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new EntityNotFoundException();
        }

        // 좋아요 정보 조회
        LikedEntity likedEntity = likedRepository.findByUserAndPost(user, post).orElse(null);

        if (likedEntity == null) {
            // 좋아요 설정
            LikedEntity newLikedEntity = new LikedEntity(user, post);
            likedRepository.save(newLikedEntity);

            // 좋아요 수 수정
            contentRepository.updateLikeCount(post.getLikeCount() + 1, postId);

            return true;
        } else {
            // 좋아요 취소
            likedRepository.delete(likedEntity);

            // 좋아요 수 수정
            contentRepository.updateLikeCount(post.getLikeCount() - 1, postId);

            return false;
        }
    }
}
