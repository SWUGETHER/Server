package com.swugether.server.domain.OCR.api;

import com.swugether.server.domain.OCR.application.OcrService;
import com.swugether.server.global.base.dto.DataResponseDto;
import com.swugether.server.global.base.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
public class OcrController {
    private final OcrService ocrService;

    @Autowired
    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    // 이미지 인식 및 텍스트 추출
    @PostMapping("/image/text")
    public ResponseEntity<ResponseDto> textFromImage(@RequestPart(value = "file") MultipartFile image) {

        List<String> result = ocrService.textFromImageService(image);

        return ResponseEntity.ok(DataResponseDto.of(result, 200));

    }

    // 이미지 인식 및 텍스트 추출
    @PostMapping("/rec")
    public ResponseEntity<ResponseDto> ocrAndRecommendation(@RequestPart(value = "file") MultipartFile image) {
        log.info("requested");

        List<String> result = ocrService.textFromImageService(image);

        return ResponseEntity.ok(DataResponseDto.of(result, 200));

    }
}
