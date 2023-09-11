package com.swugether.server.domain.OCR.api;

import com.swugether.server.domain.OCR.application.OcrService;
import com.swugether.server.global.base.constant.Code;
import com.swugether.server.global.base.dto.DataResponseDto;
import com.swugether.server.global.base.dto.ErrorResponseDto;
import com.swugether.server.global.base.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@ResponseBody
@RequestMapping("/image")
@Slf4j
public class OcrController {
    private final OcrService ocrService;

    @Autowired
    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    // 이미지 인식 및 텍스트 추출
    @PostMapping
    public ResponseEntity<ResponseDto> textFromImage(@RequestPart(value = "file") MultipartFile image) {
        try {
            String result = ocrService.textFromImageService(image);

            return ResponseEntity.status(200).body(DataResponseDto.of(result));
        } catch (Exception e) {
            log.error(e.getMessage());

            if (Objects.equals(e.getMessage(), "Type error. (Image required.)")) {
                return ResponseEntity.status(400)
                        .body(ErrorResponseDto.of(Code.BAD_REQUEST, e.getMessage()));
            }

            return ResponseEntity.status(500)
                    .body(ErrorResponseDto.of(Code.INTERNAL_ERROR, e.getMessage()));
        }
    }
}
