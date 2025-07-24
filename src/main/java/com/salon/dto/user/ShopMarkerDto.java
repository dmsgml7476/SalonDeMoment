package com.salon.dto.user;

import com.salon.entity.shop.Shop;
import com.salon.entity.shop.ShopImage;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class ShopMarkerDto {
    // 지도에 미용실 위치 표시 마커용 dto
    private Long shopId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String imgUrl;
    private String shopName;

    public static ShopMarkerDto from(Shop shop, ShopImage shopImage) {
        ShopMarkerDto dto = new ShopMarkerDto();
        dto.setShopId(shop.getId());
        dto.setLatitude(shop.getLatitude());
        dto.setLongitude(shop.getLongitude());
        dto.setImgUrl(shopImage.getImgUrl());
        dto.setShopName(shop.getName());

        return dto;
    }
}
