package com.salon.dto.admin;

import com.salon.constant.ApplyStatus;
import com.salon.entity.admin.CouponBanner;
import com.salon.entity.management.master.Coupon;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CouponBannerListDto {
    private Long id;
    private String shopName;
    private String couponName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String region;
    private ApplyStatus status;

    public String getStatusLabel(){
        return status != null ? status.getLabel() : "";
    }

    public ApplyStatus getStatus(){
        return status;
    }

    public void setStatus(ApplyStatus status){
        this.status = status;
    }

    public static CouponBannerListDto from(CouponBanner couponBanner) {
        CouponBannerListDto couponBannerListDto = new CouponBannerListDto();
        couponBannerListDto.setId(couponBanner.getId());
        couponBannerListDto.setShopName(couponBanner.getCoupon().getShop().getName());
        couponBannerListDto.setCouponName(couponBanner.getCoupon().getName());
        couponBannerListDto.setStartDate(couponBanner.getStartDate());
        couponBannerListDto.setEndDate(couponBanner.getEndDate());
        String fullAddress = couponBanner.getCoupon().getShop().getAddress();
        String[] addressParts = fullAddress.split(" ");
        String region = addressParts.length >= 2 ? addressParts[0] + " " + addressParts[1] : fullAddress;
        couponBannerListDto.setRegion(region);
        couponBannerListDto.setStatus(couponBanner.getStatus());

        return couponBannerListDto;
    }
}
