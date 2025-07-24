package com.salon.service.user;

import com.salon.constant.ServiceCategory;
import com.salon.dto.DayOffShowDto;
import com.salon.dto.management.ServiceForm;
import com.salon.dto.management.master.ShopImageDto;
import com.salon.dto.shop.ShopListDto;
import com.salon.dto.user.ShopCompareResultDto;
import com.salon.entity.shop.Shop;

import com.salon.repository.management.master.ShopServiceRepo;
import com.salon.repository.shop.ShopRepo;
import com.salon.service.management.master.CouponService;
import com.salon.service.shop.ShopImageService;

import com.salon.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompareService {
    private final ShopRepo shopRepo;
    private final ReviewService reviewService;
    private final CouponService couponService;
    private final ShopImageService shopImageService;
    private final ShopServiceRepo shopServiceRepo;

    public List<ShopCompareResultDto> getCompareResults(List<Long> shopIds, BigDecimal userLat, BigDecimal userLon) {
        List<ShopCompareResultDto> result = new ArrayList<>();

        for(Long shopId: shopIds) {
            Shop shop = shopRepo.findById(shopId)
                    .orElseThrow(() -> new IllegalArgumentException("미용실이 존재하지 않습니다"));

            float avgRating = reviewService.getAverageRatingByShop(shopId);
            int reviewCount = reviewService.getReviewCountByShop(shopId);
            boolean hasCoupon = couponService.hasActiveCoupon(shopId);
            ShopImageDto imageDto = shopImageService.findThumbnailByShopId(shopId);

            DayOffShowDto dayOffShowDto = new DayOffShowDto(shop.getDayOff());

            ShopListDto shopListDto = ShopListDto.from(shop, imageDto, avgRating, reviewCount, hasCoupon, dayOffShowDto);

            List<ServiceForm> serviceForms = shopServiceRepo.findByShopId(shopId).stream()
                    .map(ServiceForm::from)
                    .collect(Collectors.toList());

            System.out.println("🔍 shopId: " + shopId + " → 서비스 수: " + serviceForms.size());
            serviceForms.forEach(s -> System.out.println("🧾 서비스: " + s.getName() + ", 카테고리: " + s.getCategory()));


            BigDecimal distance = DistanceUtil.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
            shopListDto.setDistance(distance);

            // 카테고리별로 시술 분류

            Map<ServiceCategory, List<ServiceForm>> categorized = new EnumMap<>(ServiceCategory.class);
            for (ServiceCategory category : ServiceCategory.values()) {
                categorized.put(category,
                        serviceForms.stream()
                                .filter(s -> s.getCategory() == category)
                                .collect(Collectors.toList()));
            }

            // Dto 생성


            ShopCompareResultDto dto = ShopCompareResultDto.from(shop, shopListDto, serviceForms);
            dto.setCategorizedServices(categorized);

            result.add(dto);
        }

        return result;
    }

}
