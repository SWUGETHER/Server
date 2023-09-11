package com.swugether.server.domain.Post.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Image")
@NoArgsConstructor
@DynamicInsert
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
}
