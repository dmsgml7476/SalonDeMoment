package com.salon.dto.user;

import com.salon.entity.admin.CouponBanner;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MainCouponBannerDto {

    private Long bannerId;
    private String imgUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String region;
    private String address;
    private Long shopId;

    public static MainCouponBannerDto from(CouponBanner banner) {
        MainCouponBannerDto dto = new MainCouponBannerDto();

        dto.setBannerId(banner.getId());
        dto.setImgUrl(banner.getImgUrl());
        dto.setStartDate(banner.getStartDate());
        dto.setEndDate(banner.getEndDate());

        if (banner.getCoupon() != null && banner.getCoupon().getShop() != null) {
            String address = banner.getCoupon().getShop().getAddress();

            dto.setShopId(banner.getCoupon().getShop().getId());
            dto.setAddress(address);

            // region 추출
            if (address != null && !address.isBlank()) {
                String[] parts = address.split(" ");
                if (parts.length > 0) {
                    String rawRegion = parts[0];


                    rawRegion = rawRegion.replace("광역시", "").replace("특별시", "").replace("도", "").replace("시", "");

                    dto.setRegion(rawRegion);
                }
            }
        }
        return dto;
    }
}
