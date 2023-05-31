package com.swugether.server.db.dao;

import com.swugether.server.db.domain.ContentEntity;
import com.swugether.server.db.domain.UserEntity;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ContentRepository extends CrudRepository<ContentEntity, Long> {
  List<ContentEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);

  List<ContentEntity> findAllByOrderByCreatedAtDesc();

  List<ContentEntity> findAllByOrderByCreatedAtAsc();

  List<ContentEntity> findAllByOrderByLikeCountDesc();
}
