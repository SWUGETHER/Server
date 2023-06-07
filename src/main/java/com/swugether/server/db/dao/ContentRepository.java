package com.swugether.server.db.dao;

import com.swugether.server.db.domain.ContentEntity;
import com.swugether.server.db.domain.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface ContentRepository extends CrudRepository<ContentEntity, Long> {
    List<ContentEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);

    List<ContentEntity> findAllByOrderByCreatedAtDesc();

    List<ContentEntity> findAllByOrderByCreatedAtAsc();

    List<ContentEntity> findAllByOrderByLikeCountDesc();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE ContentEntity c SET c.likeCount = :likeCount WHERE c.id = :id")
    void updateLikeCount(@Param("likeCount") Integer likeCount, @Param("id") Long id);
}
