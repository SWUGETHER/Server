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
public class SavedPostDto {
  private Long post_id;

  @Builder
  public SavedPostDto(Long post_id) {
    this.post_id = post_id;
  }
}
