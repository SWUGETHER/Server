package com.swugether.server.db.dao;

import com.swugether.server.db.domain.LikedEntity;
import org.springframework.data.repository.CrudRepository;

public interface LikedRepository extends CrudRepository<LikedEntity, Long> {
}
