package com.salon.util;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class OcrRestUtil {
    private static final String OCR_URL = "https://vision.googleapis.com/v1/images:annotate";

    public static String extractText(MultipartFile file, String apikey) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, Object> image = Map.of("content", base64Image);
        Map<String, Object> feature = Map.of("type", "TEXT_DETECTION");
        Map<String, Object> request = Map.of("image", image, "features", List.of(feature));
        Map<String, Object> requestBody = Map.of("requests", List.of(request));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = OCR_URL + "?key=" + apikey;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        if(body == null) return "";

        Object responses0Obj = body.get("responses");
        if (!(responses0Obj instanceof List<?> responses) || responses.isEmpty()) return "";

        Object firstObj = responses.get(0);
        if(!(firstObj instanceof Map<?, ?> firstResponse)) return "";

        Object annotationObj = firstResponse.get("fullTextAnnotation");
        if(!(annotationObj instanceof Map<?, ?> annotation)) return "";

        Object textObj = annotation.get("text");
        return textObj instanceof String ? (String) textObj : "";
    }
}
