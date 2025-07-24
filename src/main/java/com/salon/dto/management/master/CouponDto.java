package com.salon.dto.management.master;

import com.salon.constant.CouponType;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.shop.Shop;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class CouponDto {

    private Long id; // Coupon ID

    private Long shopId; // Shop ID
    private String name;
    private int minimumAmount;
    private CouponType discountType;
    private int discountValue;
    private LocalDate expireDate;
    private boolean active;

    public static CouponDto from(Coupon coupon){

        CouponDto dto = new CouponDto();
        dto.setId(coupon.getId());
        dto.setShopId(coupon.getShop().getId());
        dto.setName(coupon.getName());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setMinimumAmount(coupon.getMinimumAmount());
        dto.setExpireDate(coupon.getExpireDate());
        dto.setActive(coupon.isActive());

        return dto;

    }


    public Coupon to (Shop shop) {

        Coupon coupon = new Coupon();
        coupon.setShop(shop);
        coupon.setName(this.name);
        coupon.setMinimumAmount(this.minimumAmount);
        coupon.setDiscountType(this.discountType);
        coupon.setDiscountValue(this.discountValue);
        coupon.setActive(true);
        coupon.setExpireDate(this.expireDate);

        return coupon;
    }


}
