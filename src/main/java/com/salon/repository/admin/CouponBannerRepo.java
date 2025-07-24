package com.salon.repository.admin;

import com.salon.entity.admin.CouponBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponBannerRepo extends JpaRepository<CouponBanner, Long> {

    //쿠폰 배너 가져오기

    @Query("""
SELECT b
FROM CouponBanner b
JOIN b.coupon c
JOIN c.shop s
WHERE b.status = com.salon.constant.ApplyStatus.APPROVED
AND b.startDate <= CURRENT_DATE
AND b.endDate >= CURRENT_DATE
AND s.address LIKE CONCAT(:region, '%')
""")
    List<CouponBanner> findActiveApprovedBannersByRegion(@Param("region") String region);
}
