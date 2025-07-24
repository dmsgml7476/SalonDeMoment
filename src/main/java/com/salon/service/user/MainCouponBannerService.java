package com.salon.service.user;

import com.salon.dto.user.MainCouponBannerDto;
import com.salon.repository.admin.CouponBannerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainCouponBannerService {

    private final CouponBannerRepo couponBannerRepo;


    // 해당 지역의 활성화 된 쿠폰 광고 조회
    public List<MainCouponBannerDto> getApprovedBannersByRegion(String region) {
        return couponBannerRepo.findActiveApprovedBannersByRegion(region)
                .stream()
                .map(MainCouponBannerDto::from)
                .collect(Collectors.toList());
    }

}
