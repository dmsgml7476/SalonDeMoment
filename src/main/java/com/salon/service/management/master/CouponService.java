package com.salon.service.management.master;

import com.salon.repository.management.master.CouponRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    // 일단 목록 불러올때 필요해서 만들었어요.

    private final CouponRepo couponRepo;
    // shopService 에서 사용 쿠폰 존재 여부 확인
    public boolean hasActiveCoupon(Long shopId) {
        return couponRepo.existsActiveCouponByShopId(shopId);
    }


}
