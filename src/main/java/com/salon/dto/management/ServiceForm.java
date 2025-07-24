package com.salon.dto.management;

import com.salon.constant.ServiceCategory;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.shop.Shop;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class ServiceForm {

    private Long id;
    private Long shopId;
    private String name;
    private int price;
    private String description;
    private ServiceCategory category;
    private String originalImgName;
    private String imgName;
    private String imgUrl;

    private MultipartFile imgFile;

    private boolean recommended;

    public static ServiceForm from (ShopService shopService) {

        ServiceForm dto = new ServiceForm();

        dto.setId(shopService.getId());
        dto.setShopId(shopService.getShop().getId());
        dto.setName(shopService.getName());
        dto.setPrice(shopService.getPrice());
        dto.setDescription(shopService.getDescription());
        dto.setCategory(shopService.getCategory());
        dto.setOriginalImgName(shopService.getOriginalImgName());
        dto.setImgName(shopService.getImgName());
        dto.setImgUrl(shopService.getImgUrl());
        dto.setRecommended(shopService.isRecommended());

        return dto;
    }

    public ShopService to (Shop shop) {

        ShopService shopService = new ShopService();

        shopService.setName(this.name);
        shopService.setShop(shop);
        shopService.setPrice(this.price);
        shopService.setCategory(this.category);
        shopService.setDescription(this.description);
        shopService.setRecommended(this.recommended);

        return shopService;

    }

}
