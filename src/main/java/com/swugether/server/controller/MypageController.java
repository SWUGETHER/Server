package com.swugether.server.controller;

import com.swugether.server.base.constant.Code;
import com.swugether.server.base.dto.DataResponseDto;
import com.swugether.server.base.dto.ErrorResponseDto;
import com.swugether.server.base.dto.ResponseDto;
import com.swugether.server.exception.UnauthorizedAccessException;
import com.swugether.server.service.MypageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.NoPermissionException;
import java.util.ArrayList;
import java.util.Map;

@RestController
@ResponseBody
@RequestMapping("/mypage")
@Slf4j
public class MypageController {
    private final MypageService mypageService;

    @Autowired
    public MypageController(MypageService mypageService) {
        this.mypageService = mypageService;
    }

    // 내가 쓴 글
    @GetMapping("/post")
    public ResponseEntity<ResponseDto> postList(@RequestHeader("Authorization") String bearer_token) {
        try {
            ArrayList<Map<String, Object>> list = mypageService.listService(bearer_token);

            if (list.size() == 0) {
                return ResponseEntity.status(200).body(ResponseDto.of(Code.OK, "No result."));
            } else {
                return ResponseEntity.status(200).body(DataResponseDto.of(list));
            }
        } catch (NoPermissionException | UnauthorizedAccessException e) {
            log.error(e.getMessage());

            return ResponseEntity.status(401)
                    .body(ErrorResponseDto.of(Code.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }
}
