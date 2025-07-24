package com.salon.dto.management;

import com.salon.dto.management.master.CouponDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class MemberCouponDto {
    private List<CouponDto> coupons;
    private int ticketBalance;

}
