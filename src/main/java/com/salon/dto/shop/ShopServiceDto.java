package com.salon.dto.shop;

import com.salon.constant.ServiceCategory;
import com.salon.entity.management.master.ShopService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopServiceDto {

    private Long id;
    private String name;
    private int price;
    private ServiceCategory serviceCategory;

    public static ShopServiceDto from (ShopService shopService){
        ShopServiceDto shopServiceDto = new ShopServiceDto();

        shopServiceDto.setId(shopService.getId());
        shopServiceDto.setName(shopService.getName());
        shopServiceDto.setPrice(shopService.getPrice());
        shopServiceDto.setServiceCategory(shopService.getCategory());

        return shopServiceDto;
    }
}
