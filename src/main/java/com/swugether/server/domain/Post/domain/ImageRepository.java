package com.swugether.server.domain.Post.domain;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface ImageRepository extends CrudRepository<ImageEntity, Long> {
    List<ImageEntity> findAllByPost(ContentEntity post);

    ImageEntity findTopByPostOrderByIdAsc(ContentEntity post);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE ImageEntity i SET i.imagePath = :imagePath WHERE i.id = :id")
    void updateImagePath(@Param("imagePath") String imagePath, @Param("id") Long id);
}
