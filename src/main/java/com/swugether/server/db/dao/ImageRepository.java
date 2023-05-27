package com.swugether.server.db.dao;

import com.swugether.server.db.domain.ImageEntity;
import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<ImageEntity, Long> {
}
