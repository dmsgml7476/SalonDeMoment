package com.salon.dto.management.master;

import com.salon.entity.shop.ShopImage;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ShopImageDto {

    private Long id;
    private String originalName;
    private String imgName;
    private String imgUrl;
    private Boolean isThumbnail;


    public static ShopImageDto from (ShopImage image) {
        ShopImageDto dto = new ShopImageDto();
        dto.setId(image.getId());
        dto.setImgUrl(image.getImgUrl());
        dto.setIsThumbnail(image.getIsThumbnail());

        return dto;
    }


}
