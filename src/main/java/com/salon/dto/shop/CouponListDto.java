package com.salon.dto.shop;

import com.salon.constant.CouponType;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.shop.Shop;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CouponListDto {


    private String shopName; // 미용실 이름
    private String couponName; // 쿠폰 이름
    private int minimumAmount; // 최소 예약 금액
    private CouponType couponType; // 할인 유형
    private int DiscountAmount; // 할인 금액
    private boolean isActive; // 활성화 유무
    private LocalDate expireDate; // 쿠폰 소멸일
    private Long shopId;   // 샵 아이디



    // Coupon (Entity) -> CouponListDto
    public static CouponListDto from (Coupon coupon, Shop shop){
        CouponListDto couponListDto = new CouponListDto();

        couponListDto.setShopName(shop.getName());
        couponListDto.setCouponName(coupon.getName());
        couponListDto.setMinimumAmount(coupon.getMinimumAmount());
        couponListDto.setCouponType(coupon.getDiscountType());
        couponListDto.setActive(coupon.isActive());
        couponListDto.setExpireDate(coupon.getExpireDate());
        couponListDto.setShopId(shop.getId());

        return couponListDto;
    }


}
