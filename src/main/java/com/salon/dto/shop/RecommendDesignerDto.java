package com.salon.dto.shop;


import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RecommendDesignerDto {
    private Long designerId;
    private Long shopId;
    private String designerName;
    private String shopName;
    private float rating;
    private int reviewCount;
    private String profileImgUrl;
    private String reviewImg;
    private float reviewRating;
    private String createAt;
    private String comment;
    private String position;


    public static RecommendDesignerDto from(ShopDesigner designer, float rating, int reviewCount) {
        RecommendDesignerDto dto = new RecommendDesignerDto();
        dto.setDesignerId(designer.getDesigner().getId());
        dto.setShopId(designer.getShop().getId());
        dto.setDesignerName(designer.getDesigner().getMember().getName());
        dto.setShopName(designer.getShop().getName());
        dto.setProfileImgUrl(designer.getDesigner().getImgUrl());
        dto.setRating(rating);
        dto.setReviewCount(reviewCount);
        dto.setPosition(designer.getPosition());

        return dto;
    }
}
