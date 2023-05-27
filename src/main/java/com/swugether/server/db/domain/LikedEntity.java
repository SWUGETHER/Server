package com.swugether.server.db.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "Liked")
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
