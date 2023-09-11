package com.swugether.server.global.util;

import com.swugether.server.domain.Auth.domain.UserEntity;
import com.swugether.server.domain.Post.domain.ContentEntity;
import com.swugether.server.domain.Post.domain.ImageEntity;
import com.swugether.server.domain.Post.domain.ImageRepository;
import com.swugether.server.domain.Post.domain.LikedRepository;
import com.swugether.server.domain.Post.dto.ImageDto;
import com.swugether.server.domain.Post.dto.PostDto;
import com.swugether.server.domain.Post.dto.PostItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@RequiredArgsConstructor
@Component
public class PostDtoProvider {
    private final LikedRepository likedRepository;
    private final ImageRepository imageRepository;

    // 게시글 내 이미지 목록 조회
    public ArrayList<ImageDto> getImages(ContentEntity post) {

        return imageRepository.findAllByPost(post);
    }

    // 썸네일 이미지 조회
    public String getThumbnailImagePath(ContentEntity post) {
        ImageEntity image = imageRepository.findTopByPostOrderByIdAsc(post);

        return image.getImagePath();
    }

    // postItemDto 반환
    public PostItemDto getPostItemDto(ContentEntity post, UserEntity user) {
        PostItemDto postItemDto = PostItemDto.builder()
                .post_id(post.getId())
                .updated_at(post.getUpdatedAt())
                .title(post.getTitle())
                .like_count(post.getLikeCount())
                .is_liked(likedRepository.existsByUserAndPost(user, post))
                .thumbnail_image_path(getThumbnailImagePath(post))
                .build();

        return postItemDto;
    }

    // postDto 반환
    public PostDto getPostDto(ContentEntity post, UserEntity user) {
        PostDto postDto = PostDto.builder()
                .post_id(post.getId())
                .user_id(post.getUser().getId())
                .created_at(post.getCreatedAt())
                .updated_at(post.getUpdatedAt())
                .title(post.getTitle())
                .content(post.getContent())
                .like_count(post.getLikeCount())
                .images(getImages(post))
                .is_liked(likedRepository.existsByUserAndPost(user, post))
                .build();

        return postDto;
    }
}
