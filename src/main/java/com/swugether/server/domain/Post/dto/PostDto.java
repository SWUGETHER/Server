package com.swugether.server.domain.Post.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PostDto {
    private Long post_id;
    private Long user_id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String title;
    private String content;
    private Integer like_count;
    private List<ImageDto> images;
    private Boolean is_liked;

    @Builder
    public PostDto(Long post_id, Long user_id, LocalDateTime created_at, LocalDateTime updated_at,
                   String title, String content, Integer like_count, List<ImageDto> images,
                   Boolean is_liked) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.title = title;
        this.content = content;
        this.like_count = like_count;
        this.images = images;
        this.is_liked = is_liked;
    }
}
