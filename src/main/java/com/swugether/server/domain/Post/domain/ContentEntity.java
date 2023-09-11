package com.swugether.server.domain.Post.domain;

import com.swugether.server.domain.Auth.domain.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Content")
@NoArgsConstructor
@DynamicInsert
public class ContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private UserEntity user;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(length = 100)
    @NotNull
    private String title;
    @Lob
    private String content;
    @NotNull
    private Integer likeCount;

    @Builder
    public ContentEntity(UserEntity user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.likeCount = 0;
    }
}
