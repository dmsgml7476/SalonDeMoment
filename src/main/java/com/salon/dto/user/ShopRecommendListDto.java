package com.salon.dto.user;

import com.salon.dto.management.master.ShopImageDto;
import com.salon.dto.shop.ShopListDto;
import com.salon.entity.shop.Shop;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ShopRecommendListDto {
    private Long id;
    private String shopName;
    private BigDecimal distance;
    private ShopImageDto shopImageDto;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private float avgRating;
    private int reviewCount;

    public static ShopListDto from(Shop shop, ShopImageDto shopImageDto, float avgRating, int reviewCount) {
        ShopListDto dto = new ShopListDto();

        dto.setId(shop.getId());
        dto.setShopImageDto(shopImageDto);
        dto.setShopName(shop.getName());
        dto.setRating(Math.round(avgRating * 10) / 10f);
        dto.setReviewCount(reviewCount);

        return dto;
    }



}
