package com.salon.repository.management.master;

import com.salon.entity.management.master.ShopClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShopClosedDateRepo extends JpaRepository<ShopClosedDate, Long> {

    // 미용실 특정휴무일 목록
    List<ShopClosedDate> findByShopId(Long shopId);

    // 미용실 당일 휴무 확인
    boolean existsByShopIdAndOffStartDateBetween(Long shopId, LocalDate startDate, LocalDate endDate);

}
