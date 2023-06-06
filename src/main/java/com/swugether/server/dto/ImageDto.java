package com.swugether.server.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ImageDto {
  private Long image_id;
  private String image_path;

  @Builder
  public ImageDto(Long image_id, String image_path) {
    this.image_id = image_id;
    this.image_path = image_path;
  }
}
