package com.swugether.server.db.dao;

import com.swugether.server.db.domain.ContentEntity;
import com.swugether.server.db.domain.LikedEntity;
import com.swugether.server.db.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface LikedRepository extends CrudRepository<LikedEntity, Long> {
  Boolean existsByUserAndPost(UserEntity user, ContentEntity post);

  List<LikedEntity> findAllByUser(UserEntity user);

  Optional<LikedEntity> findByUserAndPost(UserEntity user, ContentEntity post);
}
