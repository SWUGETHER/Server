package com.swugether.server.db.dao;

import com.swugether.server.db.domain.NoticeEntity;
import org.springframework.data.repository.CrudRepository;

public interface NoticeRepository extends CrudRepository<NoticeEntity, Long> {
}
