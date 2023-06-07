package com.swugether.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swugether.server.db.dao.ContentRepository;
import com.swugether.server.db.domain.ContentEntity;
import com.swugether.server.db.domain.UserEntity;
import com.swugether.server.exception.UnauthorizedAccessException;
import com.swugether.server.util.PostDtoProvider;
import com.swugether.server.util.ValidateToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.naming.NoPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MypageService {
  private final ValidateToken validateToken;
  private final PostDtoProvider postDtoProvider;
  private final ContentRepository contentRepository;
  private final ObjectMapper objectMapper;

  @Autowired
  public MypageService(ValidateToken validateToken, PostDtoProvider postDtoProvider,
      ContentRepository contentRepository, ObjectMapper objectMapper) {
    this.validateToken = validateToken;
    this.postDtoProvider = postDtoProvider;
    this.contentRepository = contentRepository;
    this.objectMapper = objectMapper;
  }

  // 내가 쓴 글 목록 조회
  public ArrayList<Map<String, Object>> listService(String authorization)
      throws IllegalStateException, UnauthorizedAccessException, NoPermissionException {
    ArrayList<Map<String, Object>> result = new ArrayList<>();

    // 토큰 유효성 검사 및 유저 정보 추출
    UserEntity user = validateToken.validateAuthorization(authorization);

    // 게시글 데이터 조회
    List<ContentEntity> contents = contentRepository.findAllByUserOrderByCreatedAtDesc(user);

    // 데이터 정제
    for (ContentEntity post : contents) {
      result.add(objectMapper.convertValue(postDtoProvider.getPostItemDto(post, user), Map.class));
    }

    return result;
  }
}
