package com.swugether.server.db.dao;

import com.swugether.server.db.domain.ContentEntity;
import com.swugether.server.db.domain.ImageEntity;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<ImageEntity, Long> {
  List<ImageEntity> findAllByPost(ContentEntity post);

  ImageEntity findTopByPostOrderByIdAsc(ContentEntity post);
}
