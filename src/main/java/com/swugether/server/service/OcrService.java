package com.swugether.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@Slf4j
public class OcrService {
    // fast api 요청
    public String postFile(MultipartFile image) throws Exception {
        String apiUrl = System.getenv("FAST_API_BASE_URL") + "/extract_data";

        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 요청 본문 설정
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", image.getResource());

            // HTTP 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // RestTemplate을 사용하여 FastAPI로 POST 요청 보내기
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            // 응답 처리
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[ " + apiUrl + " ]" + " Request successed.");

                return response.getBody();
            } else if (response.getStatusCode().value() == 422 && Objects.requireNonNull(response.getBody()).contains("value_error")) {
                log.error("Type error.");

                throw new IllegalArgumentException("Type error. (Image required.)");
            } else {
                log.error("[ " + apiUrl + " ]" + " Request failed.");

                throw new Exception("Request failed.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    // 이미지 인식
    public String textFromImageService(MultipartFile file) throws Exception {

        return postFile(file);
    }
}
