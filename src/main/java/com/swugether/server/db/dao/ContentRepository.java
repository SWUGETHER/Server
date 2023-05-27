package com.swugether.server.db.dao;

import com.swugether.server.db.domain.ContentEntity;
import org.springframework.data.repository.CrudRepository;

public interface ContentRepository extends CrudRepository<ContentEntity, Long> {
}
