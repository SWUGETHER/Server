package com.swugether.server.domain.Post.dto;

import lombok.*;

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
