package com.salon.dto.shop;

import com.salon.constant.OpenStatus;
import com.salon.dto.DayOffShowDto;
import com.salon.dto.management.master.ShopImageDto;
import com.salon.entity.shop.Shop;
import com.salon.util.DayOffUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ShopListDto {
    private Long id;
    private ShopImageDto shopImageDto;
    private String shopName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String address;
    private String addressDetail;
    private Float rating;  // 샵 전체 평균
    private int reviewCount;
    private boolean hasCoupon;
    private BigDecimal distance;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String tel;
    private List<ShopDesignerProfileDto> designerList = new ArrayList<>();
    private DayOffShowDto dayOffShowDto;
    private OpenStatus openStatus;
    private int likeCount;

    public static ShopListDto from(Shop shop, ShopImageDto shopImageDto, float avgRating, int reviewCount,
                                   boolean hasCoupon, DayOffShowDto dayOffShowDto) {
        ShopListDto dto = new ShopListDto();

        dto.setId(shop.getId());
        dto.setShopImageDto(shopImageDto);
        dto.setShopName(shop.getName());
        dto.setOpenTime(shop.getOpenTime());
        dto.setCloseTime(shop.getCloseTime());
        dto.setAddress(shop.getAddress());
        dto.setAddressDetail(shop.getAddressDetail());
        dto.setRating(Math.round(avgRating * 10) / 10f);
        dto.setReviewCount(reviewCount);
        dto.setHasCoupon(hasCoupon);
        dto.setDayOffShowDto(dayOffShowDto);
        dto.setTel(shop.getTel());


        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<DayOfWeek> offDays = DayOffUtil.decodeDayOff(shop.getDayOff());

        if (offDays.contains(today)) {
            dto.setOpenStatus(OpenStatus.DAYOFF); // 오늘은 쉬는 날
        } else {
            LocalTime now = LocalTime.now();
            if (now.isBefore(shop.getOpenTime()) || now.isAfter(shop.getCloseTime())) {
                dto.setOpenStatus(OpenStatus.CLOSED); // 시간상 종료
            } else {
                dto.setOpenStatus(OpenStatus.OPEN); // 영업중
            }
        }

        System.out.println("🟢 openStatus = " + dto.getOpenStatus());

        return dto;
    }


}
