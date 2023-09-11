package com.swugether.server.domain.MyPage.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swugether.server.domain.Auth.domain.UserEntity;
import com.swugether.server.domain.Post.domain.ContentEntity;
import com.swugether.server.domain.Post.domain.ContentRepository;
import com.swugether.server.global.exception.UnauthorizedAccessException;
import com.swugether.server.global.util.PostDtoProvider;
import com.swugether.server.global.util.ValidateToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MypageService {
    private final ValidateToken validateToken;
    private final PostDtoProvider postDtoProvider;
    private final ContentRepository contentRepository;
    private final ObjectMapper objectMapper;

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
