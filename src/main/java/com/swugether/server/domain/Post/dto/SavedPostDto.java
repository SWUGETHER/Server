package com.swugether.server.domain.Post.dto;

import lombok.*;

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
