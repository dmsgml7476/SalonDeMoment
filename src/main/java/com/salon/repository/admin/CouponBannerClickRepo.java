package com.salon.repository.admin;

import com.salon.entity.admin.CouponBannerClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponBannerClickRepo extends JpaRepository<CouponBannerClick, Long> {
}
