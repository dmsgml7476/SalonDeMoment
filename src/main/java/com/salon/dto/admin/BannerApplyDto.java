package com.salon.dto.admin;

import com.salon.entity.management.master.Coupon;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class BannerApplyDto {
    private Long couponId;
    private LocalDate startDate;
    private LocalDate endDate;
    private MultipartFile image;
}
