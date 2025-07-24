package com.salon.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ShopMapDto {
    private Long id;               // 샵 ID
    private String shopName;      // 샵 이름
    private BigDecimal latitude;  // 위도
    private BigDecimal longitude; // 경도
    private BigDecimal distance;  // 사용자와의 거리 (단위: meter)

    public ShopMapDto(Long id, String shopName, BigDecimal latitude, BigDecimal longitude, BigDecimal distance) {
        this.id = id;
        this.shopName = shopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }
}
