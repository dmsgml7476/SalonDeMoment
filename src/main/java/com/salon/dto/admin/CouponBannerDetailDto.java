package com.salon.dto.admin;

import com.salon.entity.admin.CouponBanner;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponBannerDetailDto {
    private Long id;
    private String adminName;
    private String imgUrl;
    private LocalDateTime registerDate;

    private String shopName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String region;

    public static CouponBannerDetailDto from(CouponBanner couponBanner) {
        CouponBannerDetailDto couponBannerDetailDto = new CouponBannerDetailDto();
        couponBannerDetailDto.setId(couponBanner.getId());

        if(couponBanner.getAdmin() != null){
            couponBannerDetailDto.setAdminName(couponBanner.getAdmin().getName());
        }
        couponBannerDetailDto.setImgUrl(couponBanner.getImgUrl());
        couponBannerDetailDto.setRegisterDate(couponBanner.getRegisterDate());

        couponBannerDetailDto.setShopName(couponBanner.getCoupon().getShop().getName());
        couponBannerDetailDto.setStartDate(couponBanner.getStartDate());
        couponBannerDetailDto.setEndDate(couponBanner.getEndDate());

        String fullAddress = couponBanner.getCoupon().getShop().getAddress();
        String[] addressParts = fullAddress.split(" ");
        String region = addressParts.length >= 2 ? addressParts[0] + " " + addressParts[1] : fullAddress;
        couponBannerDetailDto.setRegion(region);

        return couponBannerDetailDto;
    }
}
