package com.salon.repository.management.master;

import com.salon.constant.ServiceCategory;
import com.salon.entity.management.master.ShopService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopServiceRepo extends JpaRepository<ShopService, Long> {

    // 미용실 시술 목록
    List<ShopService> findByShopId(Long shopId);

    // 미용사 담당 시술목록 (카테고리별로)
    List<ShopService> findByShopIdAndCategoryIn(Long shopId, List<ServiceCategory> categories);

    // 미용실 추천 시술 목록
    List<ShopService> findByShopIdAndIsRecommendedTrue(Long shopId);

    // 키워드로 서비스 검색
    List<ShopService> findByNameContainingIgnoreCase(String keyword);
}
