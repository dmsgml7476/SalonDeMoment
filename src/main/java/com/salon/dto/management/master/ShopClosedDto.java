package com.salon.dto.management.master;

import com.salon.entity.management.master.ShopClosedDate;
import com.salon.entity.shop.Shop;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ShopClosedDto {

    private Long id;
    private Long shopId;
    private LocalDate offStartDate;
    private LocalDate offEndDate;
    private String reason;

    public static ShopClosedDto from (ShopClosedDate closedDate){
        ShopClosedDto dto = new ShopClosedDto();
        dto.setId(closedDate.getId());
        dto.setShopId(closedDate.getShop().getId());
        dto.setOffStartDate(closedDate.getOffStartDate());
        dto.setOffEndDate(closedDate.getOffEndDate());
        dto.setReason(closedDate.getReason());

        return dto;
    }

    public ShopClosedDate to(Shop shop){
        ShopClosedDate date = new ShopClosedDate();
        date.setShop(shop);
        date.setOffStartDate(this.offStartDate);
        date.setOffEndDate(this.offEndDate);
        date.setReason(this.reason);

        return date;
    }

}
