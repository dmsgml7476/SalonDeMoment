package com.salon.dto.shop;

import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ShopDesignerProfileDto {
    private Long designerId;
    private String imgUrl;

    public static ShopDesignerProfileDto from(ShopDesigner shopDesigner) {
        ShopDesignerProfileDto dto = new ShopDesignerProfileDto();
        dto.setDesignerId(shopDesigner.getDesigner().getId());
        dto.setImgUrl(shopDesigner.getDesigner().getImgUrl());
        return dto;
    }
}
