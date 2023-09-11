package com.swugether.server.domain.Post.domain;

import com.swugether.server.domain.Auth.domain.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Liked")
@NoArgsConstructor
@DynamicInsert
public class LikedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private UserEntity user;
    @ManyToOne(optional = false)
    private ContentEntity post;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public LikedEntity(UserEntity user, ContentEntity post) {
        this.user = user;
        this.post = post;
    }
}
