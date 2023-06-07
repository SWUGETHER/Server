package com.swugether.server.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PostItemDto {
  private Long post_id;
  private LocalDateTime updated_at;
  private String title;
  private Integer like_count;
  private Boolean is_liked;
  private String thumbnail_image_path;

  @Builder
  public PostItemDto(Long post_id, LocalDateTime updated_at, String title, Integer like_count,
      Boolean is_liked, String thumbnail_image_path) {
    this.post_id = post_id;
    this.updated_at = updated_at;
    this.title = title;
    this.like_count = like_count;
    this.is_liked = is_liked;
    this.thumbnail_image_path = thumbnail_image_path;
  }
}
