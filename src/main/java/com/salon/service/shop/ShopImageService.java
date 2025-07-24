package com.salon.service.shop;

import com.salon.dto.management.master.ShopImageDto;
import com.salon.repository.shop.ShopImageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopImageService {

    private final ShopImageRepo shopImageRepo;

    public ShopImageDto findThumbnailByShopId(Long shopId) {
        return  shopImageRepo.findByShopIdAndIsThumbnailTrue(shopId)
                .map(ShopImageDto::from)
                .orElse(null);
    }

    public ShopImageDto findFirstImageByShopId(Long shopId) {
        return shopImageRepo.findFirstByShopIdOrderByIdAsc(shopId)
                .map(ShopImageDto::from)
                .orElse(null);
    }
}
