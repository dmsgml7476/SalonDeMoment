package com.salon.service.user;

import com.fasterxml.jackson.databind.JsonNode;

import com.salon.dto.user.UserLocateDto;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.rest.api.key}")
    private String kakaoRestKey;

    private final RestTemplate kakaoRestTemplate;

    public UserLocateDto getUserAddress(BigDecimal x, BigDecimal y) {
        String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=" + x + "&y=" + y;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = kakaoRestTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class);

            JsonNode document = response.getBody().path("documents").get(0).path("address");

            String addressName = document.path("address_name").asText();
            String region1 = document.path("region_1depth_name").asText();
            String region2 = document.path("region_2depth_name").asText();

            UserLocateDto dto = new UserLocateDto();
            dto.setUserAddress(addressName);
            dto.setRegion1depth(region1);
            dto.setRegion2depth(region2);
            dto.setUserLongitude(x);
            dto.setUserLatitude(y);

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Kakao 주소 조회 실패: " + e.getMessage(), e);
        }
    }


}
