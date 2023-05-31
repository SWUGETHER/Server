package com.swugether.server.db.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Image")
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private ContentEntity post;
    @Lob
    private String imagePath;

    @Builder
    public ImageEntity(ContentEntity post, String imagePath) {
        this.post = post;
        this.imagePath = imagePath;
    }

    public ImageEntity() {

    }
}
