package com.salon.dto.user;

import com.salon.dto.DayOffShowDto;
import com.salon.entity.shop.SalonLike;
import com.salon.entity.shop.Shop;
import com.salon.entity.shop.ShopImage;
import com.salon.repository.shop.ShopImageRepo;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class LikeShopDto {
    private Long id; // 라이크 테이블 샵 아이디
    private Long shopId; // 샵 아이디
    private String imgUrl; // 샵 이미지
    private String shopName; // 샵 이름
    private String address; // 주소
    private Float rating; // 평점
    private boolean hasCoupon; // 쿠폰 유무
    private DayOffShowDto dayOffShowDto;  // 데이오프 불러오는 dto

    public static LikeShopDto from(SalonLike like, Shop shop, ShopImageRepo shopImageRepo, float avgRating, int reviewCount, boolean hasCoupon, DayOffShowDto dayOffShowDto) {
        LikeShopDto dto = new LikeShopDto();

        dto.setId(like.getId());
        dto.setShopId(like.getTypeId());
        dto.setShopName(shop.getName());

        String fullAddress = Stream.of(shop.getAddress(), shop.getAddressDetail())
                .filter(a -> a != null && !a.isBlank())
                .collect(Collectors.joining(" "));

        dto.setAddress(fullAddress);

        dto.setRating(Math.round(avgRating * 10)/10f);
        dto.setHasCoupon(hasCoupon);
        dto.setDayOffShowDto(dayOffShowDto);


        // 이미지

        ShopImage image = shopImageRepo.findByShopIdAndIsThumbnailTrue(shop.getId()).orElse(null);
        dto.setImgUrl(image != null ? image.getImgUrl() : null);

        return dto;
    }
}
